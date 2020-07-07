package com.pa4j;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import java.util.*;

import static org.bytedeco.llvm.global.LLVM.*;


/*
*
* ClassManager manages all classes and their functions,
*
* provide function to easily lookup and find
*
* */
class ClassManager {
    TypeCheckEnv env;
    CodeGenEnv cenv;

    LLVMValueRef INT0;
    LLVMValueRef BOOLFALSE;
    LLVMValueRef STRING_EMPTY;
    LLVMValueRef OBJ_NULL;
    HashSet<String> buildin_functions = new HashSet<String>();


    public ClassManager(TypeCheckEnv env, CodeGenEnv cenv){
        this.env = env;
        this.cenv = cenv;
        INT0 = LLVMConstInt(cenv.int_type, 0, 0);
        BOOLFALSE = LLVMConstInt(cenv.bool_type, 0, 0);
        STRING_EMPTY = cenv.global_text("", "STRING_EMPTY");
        OBJ_NULL = cenv.null_ptr;

        buildin_functions.add("out_int");
        buildin_functions.add("out_string");
        buildin_functions.add("in_int");
        buildin_functions.add("in_string");
        buildin_functions.add("abort");
        buildin_functions.add("main");
        buildin_functions.add("type_name");
        buildin_functions.add("copy");
    }

    public ArrayList<TypeItem> dumpAttrs(class_c c){
        ArrayList<TypeItem> arr = new ArrayList<>();
        var i = c.features.getElements().asIterator();
        while (i.hasNext()){
            TreeNode a = (TreeNode)i.next();
            TypeItem item = new TypeItem();
            if (a instanceof method) {
                var b = (method)a;
                //skip buildin method
                if ( buildin_functions.contains(b.name.str) ){
                    continue;
                }
                // record name only
                item.funTypeRef = b.code_type(this.cenv);
                item.typeRef = cenv.char_star_star; // double pointer for store function pointer
                item.isAttr = false;
                item.funSymbol = b.name;

            } else if (a instanceof attr){
                var b = (attr)a;
                item.typeRef = b.code_type(this.cenv);
                item.isAttr = true;
            }
            else {
                throw new RuntimeException("impossible here");
            }
            arr.add(item);
        }
        return arr;
    }

    private LLVMTypeRef createStruct(List<TypeItem> arr){
        var pp = new PointerPointer<LLVMTypeRef>(arr.size());
        int j = 0;
        for (var i:arr){
            pp.put(j, i.typeRef);
            j += 1;
        }
        LLVMTypeRef newtype = LLVMStructType( pp, arr.size(), 0);
        return newtype;
    }

    public void analyzeClass(class_c c){

        var citem = new ClassItem();
        classTypeMap.put(c.name, citem);

        var iter = c.features.getElements();
        var result = new LinkedList<Feature>();
        while(iter.hasMoreElements()){
            var x = (Feature)iter.nextElement();
            result.add(x);
        }
        List<AbstractSymbol> parents = env.inheritChain(c.name);
        // parents: super class --> sub-class,  Object --> c
        // build array of attrs and methods
        ArrayList<TypeItem> arr = new ArrayList<>();
        for (var p:parents){
            class_c c2 = env.findClass(p);
            var temp = this.dumpAttrs(c2);
            // only add attribute here, but collect method's names
            // TODO assume attr's name won't overlap
            arr.addAll(temp);
        }
        var attrs = new ArrayList<TypeItem>();
        var methods = new HashMap<AbstractSymbol, TypeItem>();
        for (var i:arr){
            if (i.isAttr){
                attrs.add(i);
            } else {
                methods.put(i.funSymbol, i); // the later method override current method
            }
        }

        int j = 0;
        //clear arr
        arr.clear();
        // assign index
        for (var i:attrs){
            i.storeIdx = j;
            j += 1;
            arr.add(i);
        }
        // add method
        for (var i:methods.entrySet()){
            i.getValue().storeIdx = j;
            j += 1;
            arr.add(i.getValue());
        }

        var funType = this.createStruct(arr);

        citem.typeRef = funType;
        citem.members = arr;

        cenv.DumpIR( funType );

        // init constructor
        this.CreateConstructorFunction(c);
        CreateClassCopy(c);
    }

    Map<AbstractSymbol, ClassItem> classTypeMap = new HashMap<>();

    Map<AbstractSymbol, TypeItem> symbolToType = new HashMap<>();

    public String FunctionName(class_c c, method m){
        return c.name.str + "___" + m.name.str;
    }

    public void CreateFunctions(class_c c){
        //  for function we only need to declare type and "name" of that function.

        var i = c.features.getElements().asIterator();
        ClassItem citme = this.classTypeMap.get(c);
        int j = 0;
        while (i.hasNext()){
            TreeNode a = (TreeNode)i.next();
            if (a instanceof method) {
                var b = (method)a;
                var name = FunctionName(c, b);
                //get type
                LLVMTypeRef funType = citme.members.get(j).typeRef;
                var fun = cenv.addFunction(funType, name);
                var temp = this.symbolToType.get( b.name );
                temp.funRef = fun;
                temp.funName = name;
            }
            j += 1;
        }

    }

    public void CreateClassCopy(class_c c) {
        var citem = this.classTypeMap.get(c.name);
        var pp = new PointerPointer(1).put(0, cenv.char_star);
        var fun_type = LLVMFunctionType(cenv.char_star, pp, 1, 0);

        var fun = cenv.addFunction(fun_type, "copy_" + c.name.str);
        var BB = LLVMAppendBasicBlock(fun, "");
        LLVMPositionBuilderAtEnd(cenv.builder, BB);
        // call memcpy
        var p1 = LLVMGetParam(fun, 0);

        cenv.DumpIR(citem.typeRef);//debug
        //LLVMValueRef size2 = LLVMSizeOf(fun_type);

        var size = LLVMSizeOf(citem.typeRef);
        var align = LLVMAlignOf(citem.typeRef);
        var a = 0;
        var dst = LLVMBuildAlloca(cenv.builder, citem.typeRef, "new_pointer");
        LLVMBuildMemCpy(cenv.builder, dst, a, p1, a, size);
        LLVMBuildRet(cenv.builder, dst);
        citem.copyFun = fun;
    }

    public void CreateConstructorFunction(class_c c) {
        String name = "new_" + c.name.str;
        var citem = this.classTypeMap.get(c.name);
        LLVMTypeRef retType = citem.typeRef;
        // void -> type
        var pp = new PointerPointer();
        var funType = LLVMFunctionType(retType, pp ,0,0);
        var fun = cenv.addFunction(funType, name);
        citem.constructorFun = fun;

        //debug print out fun
        cenv.DumpIR(citem.typeRef);

        cenv.DumpIR(fun);
    }

    public LLVMValueRef DefaultValue(AbstractSymbol type){
        if (type == TreeConstants.Int){
              return this.INT0;
        } else if (type == TreeConstants.Bool){
            return this.BOOLFALSE;
        } else if (type == TreeConstants.Str){
            return this.STRING_EMPTY;
        } else {
            return this.OBJ_NULL;
        }
    }

    public LLVMValueRef CreateConstructor(class_c c){
        var citem = this.classTypeMap.get(c.name);
        LLVMTypeRef type = citem.typeRef;
        // TODO need to take care of init in attr
        //  evaluate each attr not method
        var fun = citem.constructorFun;
        var BB = LLVMAppendBasicBlock(fun, "constructor");
        // need to call parent class
        LLVMPositionBuilderAtEnd(cenv.builder, BB);

        var i = c.features.getElements().asIterator();
        int j = 0;

        //TODO alloca entire class object first,
        LLVMValueRef this_p = LLVMBuildAlloca(cenv.builder, citem.typeRef, "this_p");
        //  use index to generate instruction for each index
        cenv.DumpIR(fun);

        while (i.hasNext()){
            var storeIdx = citem.members.get(j).storeIdx;
            var pp = new PointerPointer(
                    new LLVMValueRef[] { this.INT0,
                            LLVMConstInt(cenv.int_type, storeIdx, 0) });

            TreeNode a = (TreeNode)i.next();
            if (a instanceof method){
                // get the pointer of the method
                // store the pointer of method into a double pointer
                var b = (method)a;
                LLVMValueRef field_p = LLVMBuildGEP(cenv.builder,
                        this_p, pp,2,"pointer_" + b.name.str );

                TypeItem item = citem.classMethodMap.get(b.name);
                var fun_p = LLVMBuildBitCast(cenv.builder,
                        item.funRef,
                        item.funTypeRef,
                        "function_" + b.name.str +"_p");
                LLVMBuildStore(cenv.builder, fun_p, field_p);
                continue;
            }

            var b = (attr)a;

            //get pointer
            LLVMValueRef field_p = LLVMBuildGEP(cenv.builder,
                    this_p, pp,1,"pointer_" + b.name.str );

            if (b.init instanceof no_expr){
                var v = this.DefaultValue( b.type_decl );
                // TODO if object or string, need to convert type;

                LLVMBuildStore(cenv.builder, v, field_p);
            } else {
                b.init.code(cenv);
                var v = b.init.returnValue;
                // TODO no need to check type (maybe)
                LLVMBuildStore(cenv.builder, v, field_p);
            }

            j += 1;
        }
        LLVMBuildRet( cenv.builder, this_p ); // return pointer
        return fun;
    }

}

class TypeItem{
    LLVMTypeRef typeRef;
    LLVMTypeRef funTypeRef;
    LLVMValueRef funRef; // function reference
    String funName; // function reference
    AbstractSymbol funSymbol; // function reference
    int storeIdx = -1;
    boolean isAttr;

}

class ClassItem{
    LLVMTypeRef typeRef; //
    LLVMValueRef constructorFun;
    LLVMValueRef copyFun;
    ArrayList<TypeItem> members;
    Map<AbstractSymbol, TypeItem> classMethodMap = new HashMap<>();
}

