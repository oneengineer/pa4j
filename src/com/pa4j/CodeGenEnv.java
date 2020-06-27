package com.pa4j;

import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.*;
import org.bytedeco.llvm.global.LLVM.*;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.llvm.global.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.LLVMVoidType;

class StoreItem{
    int funParamIdx = -1;
    LLVMValueRef value;
    String name;

    public LLVMValueRef code(CodeGenEnv env){
        if (funParamIdx >= 0)
            return LLVMGetParam(env.currentFunctionRef, funParamIdx);
        return LLVMBuildLoad(env.builder,value,name);
    }
}

class CodeGenEnv {
    LLVMBuilderRef builder;
    LLVMModuleRef module;

    LLVMValueRef currentFunctionRef;
    Map<String, LLVMValueRef> funMap = new HashMap<>();

    LLVMTypeRef void_type = LLVMVoidType();

    Map<String, LLVMValueRef> globalText = new HashMap<>();

    //TODO make this stack like,
    Map<String, StoreItem> valueMap = new HashMap<>();

    LLVMValueRef out_int_printf_txt;
    LLVMValueRef out_string_printf_txt;
    LLVMValueRef const0_64 = LLVMConstInt(LLVMInt64Type(), 0, 1 );
    LLVMTypeRef char_star = LLVMPointerType( LLVMInt8Type(),0 );

    public CodeGenEnv(){
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeDisassembler();
        LLVMInitializeNativeTarget();

        module = LLVMModuleCreateWithName("mymodule");
        builder = LLVMCreateBuilder();
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

    public void DumpIR(LLVMValueRef v){
        LLVMDumpValue(v);
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
    }

    private void init_global_text(){
        var t1 = global_text("%d", "global_text");
        globalText.put("%d",t1);
        var t2 = global_text("%s", "global_text");
        globalText.put("%s",t2);
    }

    public LLVMTypeRef translateType(AbstractSymbol s){
        if (s == TreeConstants.Int)
            return LLVMInt32Type();

        //TODO hack object here
        if (s == TreeConstants.Object_)
            return void_type;

        //TODO hack self_type here
        if (s == TreeConstants.SELF_TYPE)
            return void_type;

        throw new RuntimeException("Do not know how to translate type");
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
