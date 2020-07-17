package com.pa4j;

import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import java.util.*;

import static org.bytedeco.llvm.global.LLVM.*;

public class StringClass extends ClassItem {

    public StringClass(CodeGenEnv cenv){
        init_attr();
        init_vtable(cenv);
        init_constructor(cenv);

    }

    private void init_attr(){
        // TODO
        attrTable = new AttrTable();
        attrTable.init_tag_vpointer();

        var item = new AttrItem();
        item.name = "string_pointer";
        item.typeRef = CodeGenEnv.char_star;

        attrTable.members.add( item );

        var item2 = new AttrItem();
        item2.name = "string_length";
        item2.typeRef = CodeGenEnv.int_type;

        attrTable.members.add( item2 );

        this.struct_typeRef = attrTable.createStruct();
    }

    private FuncItem init_length_fun(CodeGenEnv cenv){
        var length_fun = new FuncItem();
        length_fun.funSymbol = TreeConstants.length;
        length_fun.funName = "String_length";
        var length_fun_type = LLVMFunctionType( CodeGenEnv.int_type,
                new PointerPointer(new LLVMTypeRef[] { CodeGenEnv.char_star } ),1,0);
        //passed in this pointer

        length_fun.funRef = cenv.addFunction( length_fun_type, length_fun.funName );
        var bb = LLVMAppendBasicBlock(length_fun.funRef, "functionBB");
        LLVMPositionBuilderAtEnd(cenv.builder, bb);

        var this_p = LLVMGetParam(length_fun.funRef, 0);
        var struct_p = LLVMBuildBitCast(cenv.builder, this_p, struct_typeRef_pointer() , "this_p");
        cenv.DumpIR(struct_p);
        // get pointer of length parameter
        var storeIdx = 3; // the 3rd element is length
        // initialize value to pointer + idx
        var pp = new PointerPointer(
                new LLVMValueRef[] { cenv.INT0,
                        LLVMConstInt(cenv.int_type, storeIdx, 0) });
        LLVMValueRef field_p = LLVMBuildGEP(cenv.builder,
                struct_p, pp,2,"pointer_length" );
        var length = LLVMBuildLoad(cenv.builder, field_p, "length");
        LLVMBuildRet(cenv.builder, length);

        return length_fun;
    }

    private FuncItem init_copy_fun(CodeGenEnv cenv){
        // TODO maybe use default copy function
        return null;
    }

    private FuncItem init_concat_fun(CodeGenEnv cenv){
        return null;
    }

    private void init_vtable(CodeGenEnv cenv){
        //  create special copy, constructor and etc.
        virtualTable = new VirtualTable();
        var length_fun = init_length_fun(cenv);
        virtualTable.addAndReplace(length_fun);
    }

    private void init_constructor(CodeGenEnv cenv){
        // pass a char * pointer, and an integer.
        // no need to use strlen,
        String name = "new_String" ;
        LLVMTypeRef retType = LLVMPointerType(struct_typeRef,0);
        // void -> type
        var pp = new PointerPointer<LLVMTypeRef>(
                new LLVMTypeRef[]{ CodeGenEnv.char_star, CodeGenEnv.int_type });
        var funType = LLVMFunctionType(retType, pp ,2,0);
        constructorFun = cenv.addFunction(funType, name); // add function to environment

        var bb = LLVMAppendBasicBlock(constructorFun, "functionBB");
        LLVMPositionBuilderAtEnd(cenv.builder, bb);
        var p1 = LLVMGetParam(constructorFun,0);
        var p2 = LLVMGetParam(constructorFun,1);

        // alloca
        LLVMValueRef this_p = LLVMBuildAlloca(cenv.builder, struct_typeRef, "this_p");
        // get pointer

        var gep_pp1 = new PointerPointer<>( new LLVMValueRef[] {
            cenv.INT0, cenv.INT0
        } );

        var gep_pp2 = new PointerPointer<>( new LLVMValueRef[] {
                cenv.INT0, cenv.INT1
        } );
        var addr_charp = LLVMConstInBoundsGEP( this_p, gep_pp1, 2 );
        var addr_len = LLVMConstInBoundsGEP( this_p, gep_pp2, 2 );
        LLVMBuildStore(cenv.builder, p1,addr_charp);
        LLVMBuildStore(cenv.builder, p2,addr_len);

        // convert to char *
        var ret_val = LLVMBuildBitCast(cenv.builder, this_p, CodeGenEnv.char_star,"ret_value");
        LLVMBuildRet(cenv.builder, ret_val);
    }
}
