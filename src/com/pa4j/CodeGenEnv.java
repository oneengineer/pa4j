package com.pa4j;

import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.llvm.global.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.LLVMVoidType;

class StoreItem{
    int funParamIdx = -1;
    LLVMValueRef value;
    String name;
    AbstractSymbol symbol;

    public LLVMValueRef code(CodeGenEnv env){
        if (funParamIdx >= 0)
            return LLVMGetParam(env.currentFunctionRef, funParamIdx);
        return LLVMBuildLoad(env.builder,value,name);
    }
}

class CodeGenEnv {
    LLVMBuilderRef builder;
    LLVMModuleRef module;

    LLVMValueRef self_pointer;
    class_c self_class;

    FuncItem current_funcItem;

    ClassManager classManager;

    LLVMValueRef currentFunctionRef;
    Map<String, LLVMValueRef> funMap = new HashMap<>();

    SymbolTable symbolToMemory = new SymbolTable(); //AbstractSymbol map to object

    static LLVMTypeRef void_type = LLVMVoidType();
    static LLVMTypeRef int_type = LLVMInt32Type();
    static LLVMTypeRef bool_type = LLVMInt1Type();
    static LLVMTypeRef char_type = LLVMInt8Type();
    static LLVMValueRef const0_64 = LLVMConstInt(LLVMInt64Type(), 0, 1 );
    static LLVMTypeRef char_star = LLVMPointerType( LLVMInt8Type(),0 );
    static LLVMTypeRef char_star_star = LLVMPointerType( char_star,0 );

    Map<String, LLVMValueRef> globalText = new HashMap<>();

    //TODO make this stack like,

    LLVMValueRef out_int_printf_txt;
    LLVMValueRef out_string_printf_txt;




    LLVMTypeRef pointer_obj_type = this.char_star;
    LLVMValueRef null_ptr = LLVMConstPointerNull( this.char_star );


    LLVMValueRef INT0;
    LLVMValueRef INT1;
    LLVMValueRef INT2;
    LLVMValueRef BOOLFALSE;
    LLVMValueRef STRING_EMPTY;
    LLVMValueRef OBJ_NULL;

    public CodeGenEnv(){
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeDisassembler();
        LLVMInitializeNativeTarget();

        module = LLVMModuleCreateWithName("mymodule");
        builder = LLVMCreateBuilder();

        INT0 = LLVMConstInt(this.int_type, 0, 0);
        INT1 = LLVMConstInt(this.int_type, 1, 0);
        INT2 = LLVMConstInt(this.int_type, 2, 0);
        BOOLFALSE = LLVMConstInt(this.bool_type, 0, 0);
        STRING_EMPTY = this.global_text("", "STRING_EMPTY");
        OBJ_NULL = this.null_ptr;
    }

    public void DumpIR(){
        LLVMDumpModule(this.module);
    }

    public void DumpIRToFile(String path){
        var bytes = new byte[10* 1000 * 1000];
        var success = LLVMPrintModuleToFile(this.module, path, bytes);
        if (success == 0){
            System.out.println("dump IR succeed");
        } else {
            System.err.println("dump IR failed " + success);
            var s = new String(bytes, StandardCharsets.UTF_8);
            System.err.println(s);
        }
    }

    /*
    * push a map to symbol table scope
    * */
    public void PushVars(Map<AbstractSymbol, AttrItem> classTypeMap ){
        for (var i: classTypeMap.entrySet()){
            this.symbolToMemory.addId(i.getKey(), i.getValue());
        }
    }

    public void DumpIR(LLVMValueRef v){
        LLVMDumpValue(v);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void DumpIR(LLVMTypeRef v){
        var s = LLVMPrintTypeToString(v);
        System.err.println(s.getString());
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    public LLVMValueRef addFunction(LLVMTypeRef fun, String name){
        return LLVMAddFunction(module, name, fun);
    }

    public LLVMValueRef global_text(String text, String name){
        // !ADDED \0 !!!
        text += "\0";
        var type = LLVMArrayType( LLVMInt8Type(), text.length() );
        LLVMValueRef globalRef = LLVMAddGlobal(this.module, type, "global_text");
        LLVMValueRef constRef = LLVMConstString(text,text.length(), 1 );
        LLVMSetInitializer(globalRef, constRef);
        return globalRef;
    }

    public void init(){
        this.init_global_text();
        this.create_printf();
        this.init_out_int();
        this.init_out_string();
        this.init_abort_function();
        this.init_string_function();
        this.init_copy_function();


    }

    public void init_abort_function(){
        var args = new LLVMTypeRef[] { LLVMInt32Type() };
        var theType = LLVMFunctionType( this.void_type, new PointerPointer( args ), 1 , 0 );
        LLVMValueRef fun_exit = addFunction(theType, "exit");

        var args2 = new LLVMTypeRef[] {  };
        var theType2 = LLVMFunctionType( this.pointer_obj_type, new PointerPointer( args2 ), args2.length , 0 );
        LLVMValueRef fun_abort = addFunction(theType2, "abort");
        var BB = LLVMAppendBasicBlock(fun_abort,"function_BB");
        LLVMPositionBuilderAtEnd(builder, BB);
        LLVMValueRef c_neg1 = LLVMConstInt(LLVMInt32Type(), -1,1);
        PointerPointer<LLVMValueRef> params = new PointerPointer( new LLVMValueRef[] {c_neg1} ); // printf("%d", x)
        LLVMBuildCall(builder, fun_exit, params, 1, "");
        LLVMBuildRet(builder, this.null_ptr);
        //LLVMBuildRetVoid(builder);

        this.funMap.put("abort", fun_abort);
    }

    /*
    * strlen, and strcpy
    * char * strcpy ( char * destination, const char * source );
    * size_t strlen ( const char * str );
     * */
    public void init_string_function(){
        var args = new LLVMTypeRef[] { this.char_star, this.char_star };
        var theType = LLVMFunctionType( this.char_star, new PointerPointer( args ), args.length , 0 );
        LLVMValueRef strcpy = addFunction(theType, "strcpy");
        this.funMap.put("strcpy", strcpy);

        var args2 = new LLVMTypeRef[] { this.char_star };
        var theType2 = LLVMFunctionType( this.int_type, new PointerPointer( args2 ), args2.length , 0 );
        LLVMValueRef strlen = addFunction(theType2, "strlen");
        this.funMap.put("strlen", strlen);
    }

    public void init_copy_function() {
        // allocate a piece of memory, need to record length for string.
        // call strlen and then all strcpy

        var args = new LLVMTypeRef[] { this.char_star };
        var theType = LLVMFunctionType( this.char_star, new PointerPointer( args ), args.length , 0 );
        LLVMValueRef fun = addFunction(theType, "copy");
        var bb = LLVMAppendBasicBlock(fun,"function_BB");
        LLVMPositionBuilderAtEnd(builder, bb);

        var strlen = funMap.get("strlen");
        var strcpy = funMap.get("strcpy");

        var p1 = LLVMGetParam(fun,0);
        var params1 = new PointerPointer(1).put(p1);
        var len_r = LLVMBuildCall(builder, strlen, params1, 1, "len_r");
        // alloca memory
        var c1 = LLVMConstInt(this.int_type, 1, 1);
        var len_r2 = LLVMBuildAdd(builder, len_r, c1, "len_r2"); // len += 1
        var result_p = LLVMBuildArrayMalloc(builder, char_type, len_r2,"result_p");

        // call strcpy
        // TODO maybe need type conversion
        var params2 = new PointerPointer(2).put(0, p1).put(1, result_p);
        LLVMBuildCall(builder, strcpy, params2,2 ,""); // no need return value
        LLVMBuildRet(builder, result_p); // return allocated pointer
        this.funMap.put("copy", fun);
    }

    private void init_global_text(){
        var t1 = global_text("%d", "global_text");
        globalText.put("%d",t1);
        var t2 = global_text("%s", "global_text");
        globalText.put("%s",t2);
    }

    public LLVMTypeRef translateType(AbstractSymbol s){
        if (s == TreeConstants.Int)
            return this.int_type;

        if (s == TreeConstants.Bool)
            return this.bool_type; // boolean type

        if (s == TreeConstants.Object_)
            return this.pointer_obj_type;

        //TODO hack self_type here
        return this.pointer_obj_type;

        //throw new RuntimeException("Do not know how to translate type");
    }

    public void PrintCode(){
        LLVMDumpValue(this.currentFunctionRef);
    }

    /*
    * create out_init as global function
    * */
    private void init_out_int(){
        var args = new LLVMTypeRef[] { LLVMInt32Type() };
        var theType = LLVMFunctionType( LLVMInt32Type(), new PointerPointer( args ), args.length , 0 );
        LLVMValueRef fun = addFunction(theType, "out_int");

        var indicies = new PointerPointer(const0_64, const0_64); //64 bit ints
        //GEP: get element pointer
        // global variable is a pointer, string is also a point
        // this is why we need two indices to refer
        //To refer the global text like %d
        LLVMValueRef txt = globalText.get("%d");
        LLVMValueRef converted_p = LLVMConstInBoundsGEP(txt, indicies, 2); //TODO
        out_int_printf_txt = converted_p;

        var BB = LLVMAppendBasicBlock(fun,"funciton_BB");
        LLVMPositionBuilderAtEnd(builder, BB);
        var p = LLVMGetParam(fun,0);
        PointerPointer params3 = new PointerPointer(out_int_printf_txt, p); // printf("%d", x)
        var temp = LLVMBuildCall(builder,
                funMap.get("printf"),params3, 2, "call_printf");
        LLVMBuildRet(builder, temp);

        this.funMap.put("out_int", fun);
    }

    private void init_out_string(){
        var args = new LLVMTypeRef[] { char_star };
        var theType = LLVMFunctionType( LLVMInt32Type(), new PointerPointer( args ), args.length , 0 );
        LLVMValueRef fun = addFunction(theType, "out_string");

        var const0_64 = LLVMConstInt(LLVMInt64Type(), 0, 1 );
        var indicies = new PointerPointer(const0_64, const0_64); //64 bit ints
        //GEP: get element pointer
        // global variable is a pointer, string is also a point
        // this is why we need two indices to refer
        //To refer the global text like %d
        LLVMValueRef txt = globalText.get("%s");
        LLVMValueRef converted_p = LLVMConstInBoundsGEP(txt, indicies, 2); //TODO
        out_string_printf_txt = converted_p;

        var BB = LLVMAppendBasicBlock(fun,"funciton_BB");
        LLVMPositionBuilderAtEnd(builder, BB);
        var p = LLVMGetParam(fun,0);
        PointerPointer params3 = new PointerPointer(out_string_printf_txt, p); // printf("%d", x)
        var temp = LLVMBuildCall(builder,
                funMap.get("printf"),params3, 2, "call_printf");
        LLVMBuildRet(builder, temp);

        this.funMap.put("out_string", fun);
    }

    public void call_out_int(LLVMValueRef value){
        PointerPointer params3 = new PointerPointer(out_int_printf_txt, value); // printf("%d", x)
        LLVMBuildCall(builder,
                funMap.get("printf"),params3, 2, "call_printf");
    }

    public void call_out_string(LLVMValueRef global_txt_ref){
        PointerPointer params3 = new PointerPointer(out_string_printf_txt, global_txt_ref); // printf("%d", x)
        LLVMBuildCall(builder,
                funMap.get("printf"),params3, 2, "call_printf");
    }


    private void create_printf(){
        var args = new LLVMTypeRef[] { char_star };
        var theType = LLVMFunctionType( LLVMInt32Type(), new PointerPointer( args ), args.length , 1 );
        var printf = addFunction( theType, "printf" );
        funMap.put("printf", printf);
    }

}
