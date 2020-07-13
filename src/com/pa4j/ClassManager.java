package com.pa4j;

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

    public ArrayList<AttrItem> dumpAttrs(class_c c){
        ArrayList<AttrItem> arr = new ArrayList<>();
        var i = c.features.getElements().asIterator();
        while (i.hasNext()){
            TreeNode a = (TreeNode)i.next();
            if (a instanceof attr){
                AttrItem item = new AttrItem();
                var b = (attr)a;
                item.typeRef = b.code_type(this.cenv);
                arr.add(item);
            }
        }
        return arr;
    }

    public void class_method(class_c c, ClassItem citem){
        citem.classMethod = new VirtualTable();
        var t = c.features.getElements();
        while(t.hasMoreElements()){
            var i = t.nextElement();
            if (!(i instanceof method))
                continue;

            var a = (method)i;
            //skip buildin method
            if ( buildin_functions.contains(a.name.str) ){
                continue;
            }

            var item = new FuncItem();
            item.funSymbol = a.name;
            var funType = a.code_type(this.cenv);
            var name = FunctionName(c, a);
            var fun = cenv.addFunction(funType, name); // add to environment
            item.funRef = fun;
            citem.classMethod.addAndReplace(item);
        }
    }

    // generate vtable and class structure
    public void analysis_members(class_c c, ClassItem citem){
        List<AbstractSymbol> parents = env.inheritChain(c.name);
        citem.virtualTable = new VirtualTable();
        citem.attrTable = new AttrTable();
        // parents: super class --> sub-class,  Object --> c
        // build array of attrs and methods
        var attrTable = citem.attrTable;
        attrTable.init_tag_vpointer();
        for (var p:parents){
            if (p == TreeConstants.Object_)
                continue;

            class_c c2 = env.findClass(p);
            ArrayList<AttrItem> temp = this.dumpAttrs(c2);

            var item2 = this.classTypeMap.get(p);
            for (var i: item2.classMethod.members) { // for each function defined in each parent class
                citem.virtualTable.addAndReplace(i); // function pointer should be handled well
            }
            attrTable.members.addAll(temp);
        }
        attrTable.assignIndices();
    }

    /*
    * gen function types
    * gen Attr Table,  virtual Table
    * gen constructor shape
    * */
    public void analyzeClass_pass1(class_c c){
        ClassItem citem = new ClassItem();
        classTypeMap.put(c.name, citem);

        this.class_method(c, citem);

        analysis_members(c, citem);

        var struct_funType = citem.attrTable.createStruct();
        citem.struct_typeRef = struct_funType;
        cenv.DumpIR( struct_funType ); //debug

        citem.virtualTable.createTableArray(this.cenv); // create virtual table

        // init constructor
        CreateConstructor_funType(c);
        CreateConstructor_initializer(c);
        CreateClassCopy(c);
    }

    /*
    * gen constructor
    * create virtual table
    * */
    public void analyzeClass_pass2(class_c c) {
        this.CreateConstructor(c);
    }

    Map<AbstractSymbol, ClassItem> classTypeMap = new HashMap<>();

    //Map<AbstractSymbol, AttrItem> symbolToType = new HashMap<>(); // TODO no need to use

    public String FunctionName(class_c c, method m){
        return c.name.str + "___" + m.name.str;
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

        cenv.DumpIR(citem.struct_typeRef);//debug
        //LLVMValueRef size2 = LLVMSizeOf(fun_type);

        var size = LLVMSizeOf(citem.struct_typeRef);
        //var align = LLVMAlignOf(citem.struct_typeRef);
        var a = 0;
        var dst = LLVMBuildAlloca(cenv.builder, citem.struct_typeRef, "new_pointer");
        LLVMBuildMemCpy(cenv.builder, dst, a, p1, a, size);
        LLVMBuildRet(cenv.builder, dst);
        citem.copyFun = fun;
    }

    public void CreateConstructor_funType(class_c c) {
        String name = "new_" + c.name.str;
        var citem = this.classTypeMap.get(c.name);
        LLVMTypeRef retType = citem.struct_typeRef;
        // void -> type
        var pp = new PointerPointer();
        var funType = LLVMFunctionType(retType, pp ,0,0);
        var fun = cenv.addFunction(funType, name); // add function to environment
        citem.constructorFun = fun;

        cenv.DumpIR(citem.struct_typeRef);
        cenv.DumpIR(fun);

        var pp2 = new PointerPointer(1);
        pp2.put(0, CodeGenEnv.char_star);
        var init_funType = LLVMFunctionType(retType, pp ,1,0);
        name = "new_init_" + c.name.str;
        citem.constructor_initval_fun = cenv.addFunction(init_funType, name); // init value by given a pointer
        //debug print out fun

    }

    public LLVMValueRef DefaultValue(AbstractSymbol type){
        if (type == TreeConstants.Int){
              return this.INT0;
        } else if (type == TreeConstants.Bool){
            return this.BOOLFALSE;
        } else if (type == TreeConstants.Str){
            return this.STRING_EMPTY; // TODO need to handle this situation
        } else {
            return this.OBJ_NULL;
        }
    }

    public void CreateConstructor_initializer(class_c c){
        var citem = this.classTypeMap.get(c.name);
        var fun = citem.constructor_initval_fun;
        var BB = LLVMAppendBasicBlock(fun, "constructor_init");
        LLVMPositionBuilderAtEnd(cenv.builder, BB);
        // only initialize self elements.
        var this_p = LLVMGetParam(fun, 0);
        var struct_p = LLVMBuildBitCast(cenv.builder, this_p, citem.struct_typeRef , "this_p");
        var i = c.features.getElements();
        while (i.hasMoreElements()){
            var a = i.nextElement();
            if (!( a instanceof attr))
                continue;
            var b = (attr)a;


            var storeIdx = citem.attrTable.findAttrIdx(b.name);
            // initialize value to pointer + idx
            var pp = new PointerPointer(
                    new LLVMValueRef[] { this.INT0,
                            LLVMConstInt(cenv.int_type, storeIdx, 0) });
            LLVMValueRef field_p = LLVMBuildGEP(cenv.builder,
                    struct_p, pp,2,"pointer_" + b.name.str );

            if (b.init instanceof no_expr){
                var v = this.DefaultValue( b.type_decl );
                LLVMBuildStore(cenv.builder, v, field_p);
            } else {
                b.init.code(cenv);
                var v = b.init.returnValue;
                LLVMBuildStore(cenv.builder, v, field_p);
            }
        }
        citem.constructor_initval_fun = fun;
    }

    public LLVMValueRef CreateConstructor(class_c c){
        var citem = this.classTypeMap.get(c.name);
        // TODO need to take care of init in attr
        //  evaluate each attr not method
        var fun = citem.constructorFun;
        var BB = LLVMAppendBasicBlock(fun, "constructor");
        // need to call parent class
        LLVMPositionBuilderAtEnd(cenv.builder, BB);

        //alloca entire class object first,
        LLVMValueRef this_p = LLVMBuildAlloca(cenv.builder, citem.struct_typeRef, "this_p");
        //  use index to generate instruction for each index

        List<AbstractSymbol> parents = env.inheritChain(c.name);
        for (var i: parents){
            // call each class's initializer one by one, including the current class's
            var class_c2 = this.classTypeMap.get(i);
            var initval_fun = class_c2.constructor_initval_fun;

            var pp = new PointerPointer<>(1);
            pp.put(0, this_p);
            LLVMBuildCall(cenv.builder, initval_fun,
                    pp, 1, "");
        }

        // constructor alloca space first, then call init functions by one by from parent to this.


        cenv.DumpIR(fun); // debug
        LLVMBuildRet( cenv.builder, this_p ); // return pointer
        return fun;
    }

}

class AttrItem {
    LLVMTypeRef typeRef;
    int storeIdx = -1;
    String name;
    AbstractSymbol attrSymbol; // function reference
}

class FuncItem{
    LLVMTypeRef typeRef;
    LLVMValueRef funRef; // function reference
    String funName; // function reference
    AbstractSymbol funSymbol; // function reference
    class_c class_; // indicate which class's method
    int storeIdx = -1;
}

class AttrTable{
    ArrayList<AttrItem> members = new ArrayList<>(); // has to be all function type

    Map<AbstractSymbol, Integer> classAttrMap = new HashMap<>();

    int findAttrIdx(AbstractSymbol name){
        if (!classAttrMap.containsKey(name))
            throw new RuntimeException("cannot find attr " + name.str);
        return classAttrMap.get(name);
    }

    AttrItem findAttr(AbstractSymbol name){
        var i = findAttrIdx(name);
        return members.get(i);
    }

    void init_tag_vpointer(){
        AttrItem item0 = new AttrItem();
        item0.typeRef = CodeGenEnv.int_type;
        item0.name = "classTag";
        members.add(item0);

        AttrItem item1= new AttrItem();
        item1.typeRef = CodeGenEnv.char_star;
        item1.name = "vt_pointer";// virtual table pointer
        members.add(item1);
    }

    void assignIndices(){
        int j = 0;
        for (var i: members){
            i.storeIdx = j;
            j += 1;
            classAttrMap.put( i.attrSymbol, i.storeIdx );
        }
    }

    LLVMTypeRef createStruct(){
        var pp = new PointerPointer<LLVMTypeRef>(members.size());
        int j = 0;
        for (var i:members){
            pp.put(j, i.typeRef);
            j += 1;
        }
        LLVMTypeRef newtype = LLVMStructType( pp, members.size(), 0);
        return newtype;
    }
}

class VirtualTable{
    class_c class_;
    ArrayList<FuncItem> members = new ArrayList<>(); // has to be all function type
    // members' order has to follow from object to current order

    void addAndReplace(FuncItem f){
        // if exist replace
        if (classMethodMap.containsKey(f.funSymbol)){
            var old_f_idx = findMethod(f.funSymbol);
            f.storeIdx = old_f_idx;
            members.set(old_f_idx, f);
        } else {
            f.storeIdx = members.size();
            members.add(f);
        }
    }

    // function symbol to position in the table
    Map<AbstractSymbol, Integer> classMethodMap = new HashMap<>();

    int findMethod(AbstractSymbol name){
        if (classMethodMap.containsKey(name)){
            var x = classMethodMap.get(name);
            return x;
        }
        else
            throw new RuntimeException("Cannot find method" + name.str);
    }

    int findMethod(method m){
        if (classMethodMap.containsKey(m.name)){
            var x = classMethodMap.get(m.name);
            return x;
        }
        else
            throw new RuntimeException("Cannot find method" + m.name.str);
    }

    LLVMTypeRef tableType(){
        return LLVMArrayType( CodeGenEnv.char_star, members.size());
    }

    /*
    * return the global pointer array
    * */
    LLVMValueRef createTableArray(CodeGenEnv env){
        var type = tableType();
        var tb = LLVMAddGlobal(env.module, type, "VT_" + class_.name.str);
        var pointers = new PointerPointer<LLVMValueRef>( members.size() );
        int j = 0;
        for (var i:members){
            var v = i.funRef;
            // convert to char *
            var v2 = LLVMConstBitCast(v, env.char_star);
            pointers.put(j, v2);
            j += 1;
        }
        var const_arr = LLVMConstArray(CodeGenEnv.char_star, pointers , members.size());
        LLVMSetInitializer(tb, const_arr);
        return tb;
    }
}

class ClassItem{
    LLVMTypeRef struct_typeRef;
    LLVMValueRef constructorFun;
    LLVMValueRef constructor_initval_fun;
    LLVMValueRef copyFun;
    VirtualTable virtualTable;
    AttrTable attrTable;
    VirtualTable classMethod; // methid defined in class
}

