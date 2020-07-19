package com.pa4j;


import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.llvm.global.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.LLVMSetInitializer;

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
        classMethodMap.put( f.funSymbol, f.storeIdx);
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
            throw new RuntimeException("Cannot find method " + m.name.str);
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
    class_c class_;
    LLVMTypeRef struct_typeRef;
    LLVMTypeRef struct_typeRef_pointer(){
        return LLVMPointerType(this.struct_typeRef,0);
    }
    LLVMValueRef constructorFun;
    LLVMValueRef constructor_initval_fun;
    LLVMValueRef copyFun;
    VirtualTable virtualTable;
    AttrTable attrTable;
    VirtualTable classMethod; // methid defined in class


    void dumpIR_debug(CodeGenEnv cenv){
        try {

            System.out.println("--- dump " + class_.name.str +  "--- IR ----");
            System.out.println("struct_typeRef: ");Thread.sleep(10);
            cenv.DumpIR( struct_typeRef );



            System.out.println("\n---constructor: ");Thread.sleep(10);
            cenv.DumpIR(constructorFun);

            System.out.println("constructor initializer: ");Thread.sleep(10);
            cenv.DumpIR(constructor_initval_fun);

            System.out.println("\n----class method: ");
            for (var i:classMethod.members){
                System.out.println("method: " + i.funSymbol.str);Thread.sleep(10);
                cenv.DumpIR(i.funRef);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

