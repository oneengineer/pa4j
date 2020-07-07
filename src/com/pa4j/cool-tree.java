package com.pa4j;// -*- mode: java -*-
//
// file: cool-tree.m4
//
// This file defines the AST
//
//////////////////////////////////////////////////////////

import com.sun.source.tree.Tree;

import java.util.*;
import java.io.PrintStream;
import java.util.Vector;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;
import org.bytedeco.llvm.global.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

/**
 * Defines simple phylum Program
 */
abstract class Program extends TreeNode {
    protected Program(int lineNumber) {
        super(lineNumber);
    }

    public abstract void dump_with_types(PrintStream out, int n);

    public abstract void semant();

    public abstract void cgen(TypeCheckEnv env);

}


/**
 * Defines simple phylum Class_
 */
abstract class Class_ extends TreeNode {
    protected Class_(int lineNumber) {
        super(lineNumber);
    }

    public abstract void dump_with_types(PrintStream out, int n);

}


/**
 * Defines list phylum Classes
 * <p>
 * See <a href="ListNode.html">ListNode</a> for full documentation.
 */
class Classes extends ListNode {
    public final static Class elementClass = Class_.class;

    /**
     * Returns class of this lists's elements
     */
    public Class getElementClass() {
        return elementClass;
    }

    protected Classes(int lineNumber, Vector elements) {
        super(lineNumber, elements);
    }

    /**
     * Creates an empty "Classes" list
     */
    public Classes(int lineNumber) {
        super(lineNumber);
    }

    /**
     * Appends "Class_" element to this list
     */
    public Classes appendElement(TreeNode elem) {
        addElement(elem);
        return this;
    }

    public TreeNode copy() {
        return new Classes(lineNumber, copyElements());
    }
}


/**
 * Defines simple phylum Feature
 */
abstract class Feature extends TreeNode {
    protected Feature(int lineNumber) {
        super(lineNumber);
    }

    public abstract void dump_with_types(PrintStream out, int n);

}


/**
 * Defines list phylum Features
 * <p>
 * See <a href="ListNode.html">ListNode</a> for full documentation.
 */
class Features extends ListNode {
    public final static Class elementClass = Feature.class;

    /**
     * Returns class of this lists's elements
     */
    public Class getElementClass() {
        return elementClass;
    }

    protected Features(int lineNumber, Vector elements) {
        super(lineNumber, elements);
    }

    /**
     * Creates an empty "Features" list
     */
    public Features(int lineNumber) {
        super(lineNumber);
    }

    /**
     * Appends "Feature" element to this list
     */
    public Features appendElement(TreeNode elem) {
        addElement(elem);
        return this;
    }

    public TreeNode copy() {
        return new Features(lineNumber, copyElements());
    }

}


/**
 * Defines simple phylum Formal
 */
abstract class Formal extends TreeNode {
    protected Formal(int lineNumber) {
        super(lineNumber);
    }

    public abstract void dump_with_types(PrintStream out, int n);

}


/**
 * Defines list phylum Formals
 * <p>
 * See <a href="ListNode.html">ListNode</a> for full documentation.
 */
class Formals extends ListNode {
    public final static Class elementClass = Formal.class;

    /**
     * Returns class of this lists's elements
     */
    public Class getElementClass() {
        return elementClass;
    }

    protected Formals(int lineNumber, Vector elements) {
        super(lineNumber, elements);
    }

    /**
     * Creates an empty "Formals" list
     */
    public Formals(int lineNumber) {
        super(lineNumber);
    }

    /**
     * Appends "Formal" element to this list
     */
    public Formals appendElement(TreeNode elem) {
        addElement(elem);
        return this;
    }

    public TreeNode copy() {
        return new Formals(lineNumber, copyElements());
    }
}


/**
 * Defines simple phylum Expression
 */
abstract class Expression extends TreeNode {
    protected Expression(int lineNumber) {
        super(lineNumber);
    }

    private AbstractSymbol type = null;

    public AbstractSymbol get_type() {
        return type;
    }

    public Expression set_type(AbstractSymbol s) {
        type = s;
        return this;
    }

    public abstract void dump_with_types(PrintStream out, int n);

    public void dump_type(PrintStream out, int n) {
        if (type != null) {
            out.println(Utilities.pad(n) + ": " + type.getString());
        } else {
            out.println(Utilities.pad(n) + ": _no_type");
        }
    }

    public boolean isBasicType(){
        var t = get_type();
        return t == TreeConstants.Int ||
               t == TreeConstants.Str ||
               t == TreeConstants.Bool;
    }

    public void compareSemant(Expression e1, Expression e2, TypeCheckEnv env) {
        e1.semant(env);
        e2.semant(env);
        // If either <expr1> or <expr2> has static type Int, Bool, or String, then the other must have the
        //same static type.
        // Any other types, including SELF TYPE, may be freely compared. On non-basic objects,
        // equality simply checks for pointer equality

        if (e1.isBasicType() || e2.isBasicType()){
            if (e1.get_type() != e2.get_type())
                throw new RuntimeException("static type comparision requires the same type");
        }
        this.set_type(AbstractTable.BooleanTypeSymbol);
    }


    public LLVMValueRef returnValue;

    public void code(CodeGenEnv env){
        throw new RuntimeException("Not implemented code function");
    }

}


/**
 * Defines list phylum Expressions
 * <p>
 * See <a href="ListNode.html">ListNode</a> for full documentation.
 */
class Expressions extends ListNode {
    public final static Class elementClass = Expression.class;

    /**
     * Returns class of this lists's elements
     */
    public Class getElementClass() {
        return elementClass;
    }

    protected Expressions(int lineNumber, Vector elements) {
        super(lineNumber, elements);
    }

    /**
     * Creates an empty "Expressions" list
     */
    public Expressions(int lineNumber) {
        super(lineNumber);
    }

    /**
     * Appends "Expression" element to this list
     */
    public Expressions appendElement(TreeNode elem) {
        addElement(elem);
        return this;
    }

    public TreeNode copy() {
        return new Expressions(lineNumber, copyElements());
    }
}


/**
 * Defines simple phylum Case
 */
abstract class Case extends TreeNode {
    protected Case(int lineNumber) {
        super(lineNumber);
    }

    public abstract void dump_with_types(PrintStream out, int n);

}


/**
 * Defines list phylum Cases
 * <p>
 * See <a href="ListNode.html">ListNode</a> for full documentation.
 */
class Cases extends ListNode {
    public final static Class elementClass = Case.class;

    /**
     * Returns class of this lists's elements
     */
    public Class getElementClass() {
        return elementClass;
    }

    protected Cases(int lineNumber, Vector elements) {
        super(lineNumber, elements);
    }

    /**
     * Creates an empty "Cases" list
     */
    public Cases(int lineNumber) {
        super(lineNumber);
    }

    /**
     * Appends "Case" element to this list
     */
    public Cases appendElement(TreeNode elem) {
        addElement(elem);
        return this;
    }

    public TreeNode copy() {
        return new Cases(lineNumber, copyElements());
    }
}


/**
 * Defines AST constructor 'programc'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class programc extends Program {
    protected Classes classes;
    TypeCheckEnv typeEnv;

    /**
     * Creates "programc" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for classes
     */
    public programc(int lineNumber, Classes a1) {
        super(lineNumber);
        classes = a1;
    }

    public TreeNode copy() {
        return new programc(lineNumber, (Classes) classes.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "programc\n");
        classes.dump(out, n + 2);
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_program");
        for (Enumeration e = classes.getElements(); e.hasMoreElements(); ) {
            // sm: changed 'n + 1' to 'n + 2' to match changes elsewhere
            ((Class_) e.nextElement()).dump_with_types(out, n + 2);
        }
    }

    /**
     * This method is the entry point to the semantic checker.  You will
     * need to complete it in programming assignment 4.
     * <p>
     * Your checker should do the following two things:
     * <ol>
     * <li>Check that the program is semantically correct
     * <li>Decorate the abstract syntax tree with type information
     * by setting the type field in each Expression node.
     * (see tree.h)
     * </ol>
     * <p>
     * You are free to first do (1) and make sure you catch all semantic
     * errors. Part (2) can be done in a second stage when you want
     * to test the complete compiler.
     */
    public void semant() {
        /* ClassTable constructor may do some semantic analysis */
        ClassTable classTable = new ClassTable(classes);

        TypeCheckEnv env = new TypeCheckEnv();
        env.symTable = new SymbolTable();

        env.parseStage = 2; // uses all stage to parse
        env.basicStage = true;
        // CHANGED
        // init symboltable for basic types
        env.classTable = classTable;
        env.classTable.basic_cls.semant(env);

        AbstractTable.initBasics();
        env.basicStage = false;

        env.parseStage = 0; // parse 0 i.e. classes
        env.classTable.cls.semant(env);

        env.parseStage = 1; // parse 1 stage first, method declarations
        env.classTable.cls.semant(env);

        env.parseStage = 2; // parse 2 stage
        env.classTable.cls.semant(env);

        typeEnv = env;
        /* some semantic analysis code may go here */

        if (classTable.errors()) {
            System.err.println("Compilation halted due to static semantic errors.");
            System.exit(1);
        }
    }


    /*
    * generate code
    * */
    public void cgen(TypeCheckEnv env){
        // initialize code env

        var class_i = classes.getElements().asIterator();
        var cenv =  new CodeGenEnv();
        var m = new ClassManager(env, cenv);
        cenv.classManager = m;
        cenv.init();

        //create function shapes
        while (class_i.hasNext()){
            var c = (class_c)class_i.next();
            m.analyzeClass(c);

        }
        //debug
        //cenv.DumpIRToFile("out_bv.ll"); //DEBUG GEN

        cenv.symbolToMemory.enterScope(); // initial scope
        cenv.PushVars( m.symbolToType );

        //create constructor function body
        class_i = classes.getElements().asIterator();
        while (class_i.hasNext()){
            var c = (class_c)class_i.next();
            var temp_fun = m.CreateConstructor(c);
            // debug each constructor
            System.out.println("-- debug "  + c.getName() + "--");
            cenv.DumpIR(temp_fun);
        }

        class_i = classes.getElements().asIterator();
        while (class_i.hasNext()){
            var c = (class_c)class_i.next();
            cenv.self_class = c;
            c.code(cenv);
        }


        // TODO create other function body
        //cenv.DumpIR();
        var error = new BytePointer(1000 * 1000); // Used to retrieve messages from functions

        cenv.DumpIRToFile("out_bv.ll"); //DEBUG GEN
        LLVMVerifyModule(cenv.module, LLVMAbortProcessAction, error);
        cenv.DumpIRToFile("out.ll");
        //cenv.DumpIR();//debug
    }

}


/**
 * Defines AST constructor 'class_c'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class class_c extends Class_ {
    protected AbstractSymbol name;
    protected AbstractSymbol parent;
    protected Features features;
    protected AbstractSymbol filename;

    /**
     * Creates "class_c" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for name
     * @param a2         initial value for parent
     * @param a3         initial value for features
     * @param a4         initial value for filename
     */
    public class_c(int lineNumber, AbstractSymbol a1, AbstractSymbol a2, Features a3, AbstractSymbol a4) {
        super(lineNumber);
        name = a1;
        parent = a2;
        features = a3;
        filename = a4;
    }

    public TreeNode copy() {
        return new class_c(lineNumber, copy_AbstractSymbol(name), copy_AbstractSymbol(parent), (Features) features.copy(), copy_AbstractSymbol(filename));
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "class_c\n");
        dump_AbstractSymbol(out, n + 2, name);
        dump_AbstractSymbol(out, n + 2, parent);
        features.dump(out, n + 2);
        dump_AbstractSymbol(out, n + 2, filename);
    }

//    public List<Feature> collectInherentAttr(){
//        var iter = this.features.getElements();
//        var result = new LinkedList<Feature>();
//        while(iter.hasMoreElements()){
//            var x = (Feature)iter.nextElement();
//            result.add(x);
//        }
//        if (this.parent != TreeConstants.No_class){
//            var p = collectInherentAttr();
//            for (var i:p)
//                if (result.indexOf(i) == -1)
//                    result.add(i);
//        }
//        return result;
//    }

    @Override
    public void semant(TypeCheckEnv env) {
        env.symTable.enterScope(); // enter class
        env.currentClass = this;

        if (parent == AbstractTable.StringTypeSymbol)
            throw new SemanticException("Class " + this.name.getString() + " cannot inherit class String.", this);
        if (parent == AbstractTable.IntTypeSymbol)
            throw new SemanticException("Class " + this.name.getString() + " cannot inherit class Int.", this);
        if (parent == AbstractTable.BooleanTypeSymbol)
            throw new SemanticException("Class " + this.name.getString() + " cannot inherit class Bool.", this);
        if (parent == AbstractTable.SelfTypeSymbol)
            throw new SemanticException("Class " + this.name.getString() + " cannot inherit class SELF_TYPE.", this);

        if (env.parseStage >= 1)
            this.features.semant(env);
        // put class into env
        env.addClass( name, this );

        //System.out.println("class semant");
        env.symTable.exitScope(); // end class
        env.currentClass = null;
    }

    @Override
    public void code(CodeGenEnv env) {
        //TODO register a class, especially the layout

        // emit all functions
        this.features.code(env);
    }

    public AbstractSymbol getFilename() {
        return filename;
    }

    public AbstractSymbol getName() {
        return name;
    }

    public AbstractSymbol getParent() {
        return parent;
    }

    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_class");
        dump_AbstractSymbol(out, n + 2, name);
        dump_AbstractSymbol(out, n + 2, parent);
        out.print(Utilities.pad(n + 2) + "\"");
        Utilities.printEscapedString(out, filename.getString());
        out.println("\"\n" + Utilities.pad(n + 2) + "(");
        for (Enumeration e = features.getElements(); e.hasMoreElements(); ) {
            ((Feature) e.nextElement()).dump_with_types(out, n + 2);
        }
        out.println(Utilities.pad(n + 2) + ")");
    }

}


/**
 * Defines AST constructor 'method'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class method extends Feature {
    protected AbstractSymbol name;
    protected Formals formals;
    protected AbstractSymbol return_type;
    protected Expression expr;
    protected LinkedList<AbstractSymbol> symbolList;
    /**
     * Creates "method" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for name
     * @param a2         initial value for formals
     * @param a3         initial value for return_type
     * @param a4         initial value for expr
     */
    public method(int lineNumber, AbstractSymbol a1, Formals a2, AbstractSymbol a3, Expression a4) {
        super(lineNumber);
        name = a1;
        formals = a2;
        return_type = a3;
        expr = a4;
    }

    public TreeNode copy() {
        return new method(lineNumber, copy_AbstractSymbol(name), (Formals) formals.copy(), copy_AbstractSymbol(return_type), (Expression) expr.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "method\n");
        dump_AbstractSymbol(out, n + 2, name);
        formals.dump(out, n + 2);
        dump_AbstractSymbol(out, n + 2, return_type);
        expr.dump(out, n + 2);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        // create an new scope
        env.symTable.enterScope();
    
        // put parameter into scope (stack) first
        this.formals.semant(env);

        //check duplicated parameter names
        var ai = formals.getElements();
        var symbolSet = new HashSet<AbstractSymbol>();
        var symbolList = new LinkedList<AbstractSymbol>();
        while (ai.hasMoreElements()){
            var t = (formalc)ai.nextElement();
            var x = t.name;
            if (x == TreeConstants.self){
                throw new SemanticException("self cannot be parameter name", this);
            }
            if ( symbolSet.contains(x) ){
                throw new SemanticException("Formal parameter " + x.getString() + " is multiply defined.", this);
            }
            symbolSet.add(x);
            symbolList.add(t.type_decl);
        }
        this.symbolList = symbolList;

        if ( env.parseStage >= 2 ) {
            expr.semant(env);
            var returnType = expr.get_type();
            if ( !env.basicStage ) // Do not check basic classes
            {

                if (!env.checkAssignType(this.return_type, returnType))
                    throw new UnMatchTypeException(this.return_type, returnType, this);
            }
        }
        env.symTable.exitScope();


        // check override, all types have to be the same
        if (!env.basicStage) {
            var chainClassNames = env.inheritChain(env.currentClass.name);
            Collections.reverse(chainClassNames); // from parent to this
            var i = chainClassNames.iterator();
            i.next();
            while (i.hasNext()) {
                var className = i.next();
                var method_ = env.findClassMethod(className, this.name);
                if (method_ == null)
                    break;
                // make sure method_ is the same as this
                if (this.symbolList.size() != method_.symbolList.size())
                    throw new MethodOverridingException(this.name, "parameter number doesn't match", this);
                var j = this.symbolList.iterator();
                var k = method_.symbolList.iterator();
                while (j.hasNext()) {
                    var a = j.next();
                    var b = k.next();
                    if (a != b)
                        throw new MethodOverridingException(this.name, a.getString() + " type not equals to " + b.getString(), this);
                }
            }
        }

    }

    public LLVMTypeRef code_type(CodeGenEnv env){
        //TODO
        // return the type of function
        var ai = formals.getElements();
        var n = formals.getLength();
        var items = new StoreItem[n];
        var arr = new LLVMTypeRef[ n ];
        int i = 0;
        while (ai.hasMoreElements()){
            var t = (formalc)ai.nextElement();
            arr[i] = env.translateType(t.type_decl);
            var item = new StoreItem();
            item.funParamIdx = i;
            item.name = t.name.str;
            items[i] = item;
            i += 1;
        }
        LLVMTypeRef rtype = null;
        if ( this.name == TreeConstants.main_meth )
            rtype = env.void_type;
        else if ( this.name == TreeConstants.cool_abort )
            rtype = env.void_type;
        else
            rtype = env.translateType(this.return_type);
        LLVMTypeRef funtype = LLVMFunctionType(rtype, new PointerPointer(arr), n, 0);

        return funtype;
    }

    //call initialize class in main function
    private void code_main_class(CodeGenEnv env){
        //TODO call new function and set self_pointer in env for the class
        class_c c = env.self_class;
        var citem = env.classManager.classTypeMap.get(c.name);
        var t = new PointerPointer(0);
        LLVMValueRef result_p = LLVMBuildCall(env.builder, citem.constructorFun, t, 0, c.name.str + "_ptr");// return a pointer
        env.self_pointer = result_p;
    }

    @Override
    public void code(CodeGenEnv env) {
        //TODO dump a function
        //  1. create function type, register name in env
        //  2. parse body,


        LLVMTypeRef rtype = null;
        if ( this.name == TreeConstants.main_meth )
            rtype = env.void_type;
        else if ( this.name == TreeConstants.cool_abort )
            rtype = env.void_type;
        else
            rtype = env.translateType(this.return_type);
        var ai = formals.getElements();
        var n = formals.getLength();
        var items = new StoreItem[n];
        var arr = new LLVMTypeRef[ n ];
        int i = 0;
        while (ai.hasMoreElements()){
            var t = (formalc)ai.nextElement();
            arr[i] = env.translateType(t.type_decl);
            var item = new StoreItem();
            item.funParamIdx = i;
            item.name = t.name.str;
            items[i] = item;
            i += 1;
        }

        // create function type
        LLVMTypeRef funtype = LLVMFunctionType(rtype, new PointerPointer(arr), n, 0);
        // register function
        LLVMValueRef fun = env.addFunction(funtype, this.name.str);

        // register parameters into env
        for (var item: items)
            env.valueMap.put( item.name, item );
        // register this function
        // TODO use 2 pass, 1st register function, second generate code
        env.funMap.put( this.name.str, fun );
        // fun is the BB of this function
        env.currentFunctionRef = fun;
        var BB = LLVMAppendBasicBlock(fun, "function_BB");
        LLVMPositionBuilderAtEnd(env.builder, BB);

        if (this.name == TreeConstants.main_meth){
            this.code_main_class(env);
        }

        this.expr.code(env);

        if (!this.name.str.equals("main")){
            LLVMBuildRet(env.builder, this.expr.returnValue);
        } else {
            LLVMBuildRetVoid(env.builder);
        }

        //env.DumpIR();
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_method");
        dump_AbstractSymbol(out, n + 2, name);
        for (Enumeration e = formals.getElements(); e.hasMoreElements(); ) {
            ((Formal) e.nextElement()).dump_with_types(out, n + 2);
        }
        dump_AbstractSymbol(out, n + 2, return_type);
        expr.dump_with_types(out, n + 2);
    }

}


/**
 * Defines AST constructor 'attr'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class attr extends Feature {
    protected AbstractSymbol name;
    protected AbstractSymbol type_decl;
    protected Expression init;

    /**
     * Creates "attr" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for name
     * @param a2         initial value for type_decl
     * @param a3         initial value for init
     */
    public attr(int lineNumber, AbstractSymbol a1, AbstractSymbol a2, Expression a3) {
        super(lineNumber);
        name = a1;
        type_decl = a2;
        init = a3;
    }

    public TreeNode copy() {
        return new attr(lineNumber, copy_AbstractSymbol(name), copy_AbstractSymbol(type_decl), (Expression) init.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "attr\n");
        dump_AbstractSymbol(out, n + 2, name);
        dump_AbstractSymbol(out, n + 2, type_decl);
        init.dump(out, n + 2);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        // TODO make sure no duplicated defined at the same level
        if (env.symTable.probe(name) != null) {
            //TODO error message here
        }
        if (init != null) {
            init.semant(env);
            var t2 = init.get_type();
            if (t2 != type_decl) {
                //TODO error message here
            }
        }
        env.symTable.addId(name, type_decl);
    }

    public LLVMTypeRef code_type(CodeGenEnv env) {
        LLVMTypeRef typeRef = env.translateType(this.type_decl);
        return typeRef;
    }

    @Override
    public void code(CodeGenEnv env) {
        //TODO
        super.code(env);
    }

    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_attr");
        dump_AbstractSymbol(out, n + 2, name);
        dump_AbstractSymbol(out, n + 2, type_decl);
        init.dump_with_types(out, n + 2);
    }

}


/**
 * Defines AST constructor 'formalc'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class formalc extends Formal {
    protected AbstractSymbol name;
    protected AbstractSymbol type_decl;

    /**
     * Creates "formalc" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for name
     * @param a2         initial value for type_decl
     */
    public formalc(int lineNumber, AbstractSymbol a1, AbstractSymbol a2) {
        super(lineNumber);
        name = a1;
        type_decl = a2;
    }

    public TreeNode copy() {
        return new formalc(lineNumber, copy_AbstractSymbol(name), copy_AbstractSymbol(type_decl));
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "formalc\n");
        dump_AbstractSymbol(out, n + 2, name);
        dump_AbstractSymbol(out, n + 2, type_decl);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        env.symTable.addId(name, type_decl);
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_formal");
        dump_AbstractSymbol(out, n + 2, name);
        dump_AbstractSymbol(out, n + 2, type_decl);
    }

}


/**
 * Defines AST constructor 'branch'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class branch extends Case {
    protected AbstractSymbol name;
    protected AbstractSymbol type_decl;
    protected Expression expr;

    /**
     * Creates "branch" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for name
     * @param a2         initial value for type_decl
     * @param a3         initial value for expr
     */
    public branch(int lineNumber, AbstractSymbol a1, AbstractSymbol a2, Expression a3) {
        super(lineNumber);
        name = a1;
        type_decl = a2;
        expr = a3;
    }

    public TreeNode copy() {
        return new branch(lineNumber, copy_AbstractSymbol(name), copy_AbstractSymbol(type_decl), (Expression) expr.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "branch\n");
        dump_AbstractSymbol(out, n + 2, name);
        dump_AbstractSymbol(out, n + 2, type_decl);
        expr.dump(out, n + 2);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        // name:type_decl => expr

        // add name into environment
        env.symTable.enterScope();
        env.symTable.addId(name, type_decl);
        expr.semant(env);
        env.symTable.exitScope();
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_branch");
        dump_AbstractSymbol(out, n + 2, name);
        dump_AbstractSymbol(out, n + 2, type_decl);
        expr.dump_with_types(out, n + 2);
    }

}


/**
 * Defines AST constructor 'assign'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class assign extends Expression {
    protected AbstractSymbol name;
    protected Expression expr;

    /**
     * Creates "assign" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for name
     * @param a2         initial value for expr
     */
    public assign(int lineNumber, AbstractSymbol a1, Expression a2) {
        super(lineNumber);
        name = a1;
        expr = a2;
    }

    public TreeNode copy() {
        return new assign(lineNumber, copy_AbstractSymbol(name), (Expression) expr.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "assign\n");
        dump_AbstractSymbol(out, n + 2, name);
        expr.dump(out, n + 2);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        var type = (AbstractSymbol) env.symTable.lookup(name);
        if (type == null){
            // find it from inherent class
            var attr = env.findClassAttr(env.currentClass, name);
            if (attr == null)
                throw new UndefinedSymbolException(name, this);
            type = attr.type_decl;
        }
        expr.semant(env);

        var exprtype = expr.get_type();
        if ( !env.checkAssignType(type, exprtype) ) {
            throw new UnMatchTypeException(type, exprtype, this);
        }
        this.set_type( type );
    }

    @Override
    public void code(CodeGenEnv env) {
        this.expr.code(env);
        var valueAddr = env.valueMap.get(this.name.str).value;
        LLVMBuildStore(env.builder, expr.returnValue, valueAddr);
        this.returnValue = expr.returnValue;
    }

    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_assign");
        dump_AbstractSymbol(out, n + 2, name);
        expr.dump_with_types(out, n + 2);
        dump_type(out, n);
    }

}


/**
 * Defines AST constructor 'static_dispatch'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class static_dispatch extends Expression {
    protected Expression expr;
    protected AbstractSymbol type_name;
    protected AbstractSymbol name;
    protected Expressions actual;

    /**
     * Creates "static_dispatch" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for expr
     * @param a2         initial value for type_name
     * @param a3         initial value for name
     * @param a4         initial value for actual
     */
    public static_dispatch(int lineNumber, Expression a1, AbstractSymbol a2, AbstractSymbol a3, Expressions a4) {
        super(lineNumber);
        expr = a1;
        type_name = a2;
        name = a3;
        actual = a4;
    }

    public TreeNode copy() {
        return new static_dispatch(lineNumber, (Expression) expr.copy(), copy_AbstractSymbol(type_name), copy_AbstractSymbol(name), (Expressions) actual.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "static_dispatch\n");
        expr.dump(out, n + 2);
        dump_AbstractSymbol(out, n + 2, type_name);
        dump_AbstractSymbol(out, n + 2, name);
        actual.dump(out, n + 2);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        // "expr@type_name.name( actual:_* )"

        expr.semant(env);

        actual.semant(env); // TODO check actual types

        if (env.findClass(this.type_name) == null) // make sure class exist
        {
            throw new RuntimeException("cannot find class:" + this.type_name.getString());
        }

        //env.enterCall(expr.get_type());

        // expr has specially case of `self`  the same as this
        AbstractSymbol exprType;
        // No need to check whether exprType is self_type or not, because it is specified as this.type_name
        // but exprType is still needed, in the case of dispatch's return type is self_type
        exprType = expr.get_type();

        // check exprType and this.type_name, just like assign type_name <- exprType
        if ( !env.checkAssignType(this.type_name, exprType) ){
            throw new SemanticException("Cannot convert " + exprType.getString() + " to " + this.type_name.getString(), this);
        }

        var method = env.findClassMethod( this.type_name,  name); // use this.type_name to find method
        var return_type = method.return_type;

        // based on exprType and return Type, decide final dispatch method type

        // self type should return `class` in the environment
        if (return_type == TreeConstants.SELF_TYPE)
            this.set_type( exprType );
        else
            this.set_type( return_type );

    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_static_dispatch");
        expr.dump_with_types(out, n + 2);
        dump_AbstractSymbol(out, n + 2, type_name);
        dump_AbstractSymbol(out, n + 2, name);
        out.println(Utilities.pad(n + 2) + "(");
        for (Enumeration e = actual.getElements(); e.hasMoreElements(); ) {
            ((Expression) e.nextElement()).dump_with_types(out, n + 2);
        }
        out.println(Utilities.pad(n + 2) + ")");
        dump_type(out, n);
    }

}


/**
 * Defines AST constructor 'dispatch'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class dispatch extends Expression {
    protected Expression expr;
    protected AbstractSymbol name;
    protected Expressions actual;

    /**
     * Creates "dispatch" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for expr
     * @param a2         initial value for name
     * @param a3         initial value for actual
     */
    public dispatch(int lineNumber, Expression a1, AbstractSymbol a2, Expressions a3) {
        super(lineNumber);
        expr = a1;
        name = a2;
        actual = a3;
    }

    public TreeNode copy() {
        return new dispatch(lineNumber, (Expression) expr.copy(), copy_AbstractSymbol(name), (Expressions) actual.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "dispatch\n");
        expr.dump(out, n + 2);
        dump_AbstractSymbol(out, n + 2, name);
        actual.dump(out, n + 2);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        //TODO check parameters' types
        //TODO handle self-type, check parameter types
        //TODO find e0's class, find method's return_type
        // "expr.name( actual:_* )"

        expr.semant(env);

        actual.semant(env); // TODO need to check parameter type

        //env.enterCall(expr.get_type());

        // expr has specially case of `self`  the same as this
        AbstractSymbol exprType;
        if (expr.get_type() == TreeConstants.SELF_TYPE){
            exprType = env.currentClass.name;
        }
        else {
            exprType = expr.get_type();
        }

        var method = env.findClassMethod( exprType,  name);
        var return_type = method.return_type;

        // based on exprType and return Type, decide final dispatch method type

        // self type should return `class` in the environment
        if (return_type == AbstractTable.SelfTypeSymbol)
            this.set_type( expr.get_type() );
        else
            this.set_type( return_type );

    }

    @Override
    public void code(CodeGenEnv env) {
        //TODO treat buildin function specially
        var functionName = this.name.str;
        //System.out.println("functionName: "  +  functionName);

        //TODO handle self_type and inherit properly
        //expr.code(env);
        var type = env.translateType(this.get_type());
        actual.code(env); // TODO need to check parameter type

        var n = actual.getLength();
        var ai = actual.getElements();

        boolean contains_expr_pointer = true;
        var buildin_functions = new HashSet<String>();
        buildin_functions.add("out_int");
        buildin_functions.add("out_string");
        buildin_functions.add("abort");
        buildin_functions.add("main");

        if (buildin_functions.contains(functionName))
            contains_expr_pointer = false;

        if (contains_expr_pointer){
            n += 1;
        }

        var arr = new LLVMValueRef[n];
        var i = 0;
        if (contains_expr_pointer){
            LLVMValueRef ref;
            if ( expr instanceof object && ((object)expr).name == TreeConstants.self ){
                ref = env.self_pointer; // this pointer
            } else{
                expr.code(env);
                ref = expr.returnValue;
            }
            // convert it to char *
            var ref2 = LLVMBuildBitCast(env.builder, ref, env.char_star, "converted_type");
            arr[i] = ref2;
            i +=1;
        }
        while (ai.hasMoreElements()){
            var expr = (Expression)ai.nextElement();
            arr[i] = expr.returnValue;
            i += 1;
        }

//        if (this.name.str.equals("out_int")){
//            env.call_out_int(arr[0]);
//            return;
//        }
//
//        if (this.name.str.equals("out_string")){
//            env.call_out_string(arr[0]);
//            return;
//        }

        // call function
        //TODO use abstract Symbol as map key, instead of string

        LLVMValueRef fun = env.funMap.get(functionName);

        if (fun == null){
            throw new RuntimeException("Cannot find "+ functionName);
        }

        //TODO hack out_int,  abort return type here
        if (functionName.equals("out_int") || functionName.equals("out_string")){
            type = env.int_type;
        }

        if ( type == env.void_type ) { // void return type
            LLVMBuildCall(env.builder, fun,
                    new PointerPointer(arr), n, "");
        } else {
            this.returnValue = LLVMBuildCall(env.builder, fun,
                    new PointerPointer(arr), n, "ret_r");
        }

    }

    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_dispatch");
        expr.dump_with_types(out, n + 2);
        dump_AbstractSymbol(out, n + 2, name);
        out.println(Utilities.pad(n + 2) + "(");
        for (Enumeration e = actual.getElements(); e.hasMoreElements(); ) {
            ((Expression) e.nextElement()).dump_with_types(out, n + 2);
        }
        out.println(Utilities.pad(n + 2) + ")");
        dump_type(out, n);
    }

}


/**
 * Defines AST constructor 'cond'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class cond extends Expression {
    protected Expression pred;
    protected Expression then_exp;
    protected Expression else_exp;

    /**
     * Creates "cond" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for pred
     * @param a2         initial value for then_exp
     * @param a3         initial value for else_exp
     */
    public cond(int lineNumber, Expression a1, Expression a2, Expression a3) {
        super(lineNumber);
        pred = a1;
        then_exp = a2;
        else_exp = a3;
    }

    public TreeNode copy() {
        return new cond(lineNumber, (Expression) pred.copy(), (Expression) then_exp.copy(), (Expression) else_exp.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "cond\n");
        pred.dump(out, n + 2);
        then_exp.dump(out, n + 2);
        else_exp.dump(out, n + 2);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        pred.semant(env);

        // pred should have boolean type
        if (pred.get_type() != AbstractTable.BooleanTypeSymbol)
            throw new RuntimeException("pred is not boolean type" );
        then_exp.semant(env);
        else_exp.semant(env);

        // get join type;
        var joinedType = env.joinedType(then_exp.get_type(), else_exp.get_type());

        this.set_type(joinedType);
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_cond");
        pred.dump_with_types(out, n + 2);
        then_exp.dump_with_types(out, n + 2);
        else_exp.dump_with_types(out, n + 2);
        dump_type(out, n);
    }

    @Override
    public void code(CodeGenEnv env) {
        // if pred then then_expr, else else_expr fi
        // create 2 blocks, then block and else block
        //LLVMBasicBlockRef ifBB = LLVMAppendBasicBlock(env.currentFunctionRef, "if_BB");
        LLVMBasicBlockRef thenBB = LLVMAppendBasicBlock(env.currentFunctionRef, "then_BB");
        LLVMBasicBlockRef elseBB = LLVMAppendBasicBlock(env.currentFunctionRef, "else_BB");
        LLVMBasicBlockRef endBB = LLVMAppendBasicBlock(env.currentFunctionRef, "endBB");

        var returnType = env.translateType(this.get_type());
        var resultAddr = LLVMBuildAlloca(env.builder, returnType, "ifelse_r_addr");

        var this_type = env.translateType( this.get_type() );
        var then_type = env.translateType( then_exp.get_type() );
        var else_type = env.translateType( else_exp.get_type() );

        this.pred.code(env);

        //set pointer to then BB
        var predResult = pred.returnValue;
        LLVMBuildCondBr(env.builder, predResult, thenBB, elseBB);

        LLVMPositionBuilderAtEnd(env.builder, thenBB);
        this.then_exp.code(env);
        //type conversion if type is different
        if (this_type != then_type){
            var temp = LLVMBuildBitCast(env.builder, then_exp.returnValue, this_type, "converted");
            LLVMBuildStore(env.builder, temp, resultAddr);
        } else
            LLVMBuildStore(env.builder, then_exp.returnValue, resultAddr);


        LLVMBuildBr(env.builder, endBB); // jmp to end

        LLVMPositionBuilderAtEnd(env.builder, elseBB);
        this.else_exp.code(env);
        //type conversion if type is different
        if (this_type != else_type){
            var temp = LLVMBuildBitCast(env.builder, else_exp.returnValue, this_type, "converted");
            LLVMBuildStore(env.builder, temp, resultAddr);
        } else
            LLVMBuildStore(env.builder, else_exp.returnValue, resultAddr);

        LLVMBuildBr(env.builder, endBB); // jmp to end
        LLVMPositionBuilderAtEnd(env.builder, endBB);

//        System.out.println("\nthen BB --------- ");
//        env.DumpIR(this.then_exp.returnValue);
//        System.out.println("\nelse BB --------- ");
//        env.DumpIR(this.else_exp.returnValue);

        var result_r = LLVMBuildLoad(env.builder, resultAddr, "ifelse_r");

        this.returnValue = result_r;
//        System.out.println("\nend BB --------- ");
//        env.DumpIR(result_r);
//        System.out.println("\n ---- if else end ----- ");
    }

}


/**
 * Defines AST constructor 'loop'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class loop extends Expression {
    protected Expression pred;
    protected Expression body;

    /**
     * Creates "loop" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for pred
     * @param a2         initial value for body
     */
    public loop(int lineNumber, Expression a1, Expression a2) {
        super(lineNumber);
        pred = a1;
        body = a2;
    }

    public TreeNode copy() {
        return new loop(lineNumber, (Expression) pred.copy(), (Expression) body.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "loop\n");
        pred.dump(out, n + 2);
        body.dump(out, n + 2);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        pred.semant(env);
        if (pred.get_type() != TreeConstants.Bool){
            throw new RuntimeException("loop condition expect a boolean type");
        }
        body.semant(env);
        this.set_type( TreeConstants.Object_ );
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_loop");
        pred.dump_with_types(out, n + 2);
        body.dump_with_types(out, n + 2);
        dump_type(out, n);
    }

}


/**
 * Defines AST constructor 'typcase'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class typcase extends Expression {
    protected Expression expr;
    protected Cases cases;

    @Override
    public void semant(TypeCheckEnv env) {
        // case expr of
        // branch1 ...
        // branch2 ... (x:T => expr)
        // esac
        expr.semant(env);

        cases.semant(env);
        // type of cases expression is the joined type of each case clause
        var i = cases.getElements();
        AbstractSymbol joinedType = null;
        while (i.hasMoreElements()){
            var j = (branch)i.nextElement();
            // we dont' care j.type_decl here
            j.semant(env);
            if (joinedType == null){
                joinedType = j.expr.get_type();
            } else {
                joinedType = env.joinedType(joinedType, j.expr.get_type());
            }
            System.out.println(j.toString());
        }
        // return type is the joined type of each branch
        this.set_type(joinedType);
    }

    /**
     * Creates "typcase" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for expr
     * @param a2         initial value for cases
     */
    public typcase(int lineNumber, Expression a1, Cases a2) {
        super(lineNumber);
        expr = a1;
        cases = a2;
    }

    public TreeNode copy() {
        return new typcase(lineNumber, (Expression) expr.copy(), (Cases) cases.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "typcase\n");
        expr.dump(out, n + 2);
        cases.dump(out, n + 2);
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_typcase");
        expr.dump_with_types(out, n + 2);
        for (Enumeration e = cases.getElements(); e.hasMoreElements(); ) {
            ((Case) e.nextElement()).dump_with_types(out, n + 2);
        }
        dump_type(out, n);
    }

}


/**
 * Defines AST constructor 'block'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class block extends Expression {
    protected Expressions body;
    public AbstractSymbol type = null;

    /**
     * Creates "block" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for body
     */
    public block(int lineNumber, Expressions a1) {
        super(lineNumber);
        body = a1;
    }

    public TreeNode copy() {
        return new block(lineNumber, (Expressions) body.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "block\n");
        body.dump(out, n + 2);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        env.symTable.enterScope();
        // use the last expression as
        this.body.semant(env);
        if (this.body.getLength() > 0) {
            var e = (Expression) this.body.last();
            this.set_type( e.get_type() );
            //TODO return last value, type should be last value's type
        }
        else {

            //TODO throw exception, because the body should have at least only one
        }
        env.symTable.exitScope();
    }

    @Override
    public void code(CodeGenEnv env) {
        this.body.code(env);
    }

    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_block");
        for (Enumeration e = body.getElements(); e.hasMoreElements(); ) {
            ((Expression) e.nextElement()).dump_with_types(out, n + 2);
        }
        dump_type(out, n);
    }

}


/**
 * Defines AST constructor 'let'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class let extends Expression {
    protected AbstractSymbol identifier;
    protected AbstractSymbol type_decl;
    protected Expression init;
    protected Expression body;

    /**
     * Creates "let" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for identifier
     * @param a2         initial value for type_decl
     * @param a3         initial value for init
     * @param a4         initial value for body
     */
    public let(int lineNumber, AbstractSymbol a1, AbstractSymbol a2, Expression a3, Expression a4) {
        super(lineNumber);
        identifier = a1;
        type_decl = a2;
        init = a3;
        body = a4;
    }

    public TreeNode copy() {
        return new let(lineNumber, copy_AbstractSymbol(identifier), copy_AbstractSymbol(type_decl), (Expression) init.copy(), (Expression) body.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "let\n");
        dump_AbstractSymbol(out, n + 2, identifier);
        dump_AbstractSymbol(out, n + 2, type_decl);
        init.dump(out, n + 2);
        body.dump(out, n + 2);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        //super.semant(env);
        //TODO it is possible to have ' let x:SELF_TYPE '
        // identifier : type_decl <- init in body
        if (identifier == TreeConstants.self)
            throw new SemanticException("'self' cannot be bound in a 'let' expression.", this);

        AbstractSymbol type_;
//        if (type_decl == AbstractTable.SelfTypeSymbol){
//            type_ = env.currentClass.getName();
//        } else {
            type_ = type_decl;
        //}
        if (!(init instanceof no_expr)) {
            init.semant(env);
            var init_type = init.get_type();
            if (!env.checkAssignType(type_, init_type)) {
                throw new UnMatchTypeException(type_, init_type, this);
            }
        }
        env.symTable.enterScope();
        env.symTable.addId(identifier, type_);

        body.semant(env);
        env.symTable.exitScope();
        this.set_type( body.get_type() );
    }

    @Override
    public void code(CodeGenEnv env) {
        //TODO push scope, add value map
        var type = env.translateType(this.type_decl);
        LLVMValueRef value_addr = LLVMBuildAlloca(env.builder, type, this.identifier.str);
        var item = new StoreItem();
        item.name = this.identifier.str;
        item.value = value_addr;
        env.valueMap.put(item.name, item);
        this.body.code(env);
    }

    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_let");
        dump_AbstractSymbol(out, n + 2, identifier);
        dump_AbstractSymbol(out, n + 2, type_decl);
        init.dump_with_types(out, n + 2);
        body.dump_with_types(out, n + 2);
        dump_type(out, n);
    }

}


/**
 * Defines AST constructor 'plus'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class plus extends Expression {
    protected Expression e1;
    protected Expression e2;

    /**
     * Creates "plus" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for e1
     * @param a2         initial value for e2
     */
    public plus(int lineNumber, Expression a1, Expression a2) {
        super(lineNumber);
        e1 = a1;
        e2 = a2;
    }

    public TreeNode copy() {
        return new plus(lineNumber, (Expression) e1.copy(), (Expression) e2.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "plus\n");
        e1.dump(out, n + 2);
        e2.dump(out, n + 2);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        e1.semant(env);
        e2.semant(env);
        if (e1.get_type() != AbstractTable.IntTypeSymbol ||
                e2.get_type() != AbstractTable.IntTypeSymbol )
            throw new SemanticException("Cannot plus expression doesn't have int type", this);
        this.set_type(AbstractTable.IntTypeSymbol);
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_plus");
        e1.dump_with_types(out, n + 2);
        e2.dump_with_types(out, n + 2);
        dump_type(out, n);
    }

    @Override
    public void code(CodeGenEnv env) {
        e1.code(env);
        e2.code(env);
        var result = LLVMBuildAdd(env.builder, e1.returnValue, e2.returnValue, "add_r");
        this.returnValue = result;
    }

}


/**
 * Defines AST constructor 'sub'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class sub extends Expression {
    protected Expression e1;
    protected Expression e2;

    /**
     * Creates "sub" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for e1
     * @param a2         initial value for e2
     */
    public sub(int lineNumber, Expression a1, Expression a2) {
        super(lineNumber);
        e1 = a1;
        e2 = a2;
    }

    public TreeNode copy() {
        return new sub(lineNumber, (Expression) e1.copy(), (Expression) e2.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "sub\n");
        e1.dump(out, n + 2);
        e2.dump(out, n + 2);
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_sub");
        e1.dump_with_types(out, n + 2);
        e2.dump_with_types(out, n + 2);
        dump_type(out, n);
    }

    @Override
    public void code(CodeGenEnv env) {
        e1.code(env);
        e2.code(env);
        var result = LLVMBuildSub(env.builder, e1.returnValue, e2.returnValue, "sub_r");
        this.returnValue = result;
    }

    @Override
    public void semant(TypeCheckEnv env) {
        e1.semant(env);
        e2.semant(env);
        if (e1.get_type() != AbstractTable.IntTypeSymbol ||
                e2.get_type() != AbstractTable.IntTypeSymbol )
            throw new SemanticException("Cannot sub expression doesn't have int type", this);
        this.set_type(AbstractTable.IntTypeSymbol);
    }
}


/**
 * Defines AST constructor 'mul'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class mul extends Expression {
    protected Expression e1;
    protected Expression e2;

    /**
     * Creates "mul" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for e1
     * @param a2         initial value for e2
     */
    public mul(int lineNumber, Expression a1, Expression a2) {
        super(lineNumber);
        e1 = a1;
        e2 = a2;
    }

    public TreeNode copy() {
        return new mul(lineNumber, (Expression) e1.copy(), (Expression) e2.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "mul\n");
        e1.dump(out, n + 2);
        e2.dump(out, n + 2);
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_mul");
        e1.dump_with_types(out, n + 2);
        e2.dump_with_types(out, n + 2);
        dump_type(out, n);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        e1.semant(env);
        e2.semant(env);
        if (e1.get_type() != AbstractTable.IntTypeSymbol ||
                e2.get_type() != AbstractTable.IntTypeSymbol )
            throw new SemanticException("Cannot mul expression doesn't have int type", this);
        this.set_type(AbstractTable.IntTypeSymbol);
    }

    @Override
    public void code(CodeGenEnv env) {
        e1.code(env);
        e2.code(env);
        this.returnValue = LLVMBuildMul( env.builder, e1.returnValue, e2.returnValue, "mul");
    }
}


/**
 * Defines AST constructor 'divide'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class divide extends Expression {
    protected Expression e1;
    protected Expression e2;

    /**
     * Creates "divide" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for e1
     * @param a2         initial value for e2
     */
    public divide(int lineNumber, Expression a1, Expression a2) {
        super(lineNumber);
        e1 = a1;
        e2 = a2;
    }

    public TreeNode copy() {
        return new divide(lineNumber, (Expression) e1.copy(), (Expression) e2.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "divide\n");
        e1.dump(out, n + 2);
        e2.dump(out, n + 2);
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_divide");
        e1.dump_with_types(out, n + 2);
        e2.dump_with_types(out, n + 2);
        dump_type(out, n);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        e1.semant(env);
        e2.semant(env);
        if (e1.get_type() != AbstractTable.IntTypeSymbol ||
                e2.get_type() != AbstractTable.IntTypeSymbol )
            throw new SemanticException("Cannot divide expression doesn't have int type", this);
        this.set_type(AbstractTable.IntTypeSymbol);
    }

    @Override
    public void code(CodeGenEnv env) {
        e1.code(env);
        e2.code(env);
        this.returnValue = LLVMBuildSDiv( env.builder, e1.returnValue, e2.returnValue, "div");
    }
}


/**
 * Defines AST constructor 'neg'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class neg extends Expression {
    protected Expression e1;

    /**
     * Creates "neg" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for e1
     */
    public neg(int lineNumber, Expression a1) {
        super(lineNumber);
        e1 = a1;
    }

    public TreeNode copy() {
        return new neg(lineNumber, (Expression) e1.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "neg\n");
        e1.dump(out, n + 2);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        e1.semant(env);
        this.set_type(e1.get_type());
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_neg");
        e1.dump_with_types(out, n + 2);
        dump_type(out, n);
    }

}


/**
 * Defines AST constructor 'lt'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class lt extends Expression {
    protected Expression e1;
    protected Expression e2;

    /**
     * Creates "lt" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for e1
     * @param a2         initial value for e2
     */
    public lt(int lineNumber, Expression a1, Expression a2) {
        super(lineNumber);
        e1 = a1;
        e2 = a2;
    }

    public TreeNode copy() {
        return new lt(lineNumber, (Expression) e1.copy(), (Expression) e2.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "lt\n");
        e1.dump(out, n + 2);
        e2.dump(out, n + 2);
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_lt");
        e1.dump_with_types(out, n + 2);
        e2.dump_with_types(out, n + 2);
        dump_type(out, n);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        this.compareSemant(e1, e2, env);
    }

    @Override
    public void code(CodeGenEnv env) {
        e1.code(env);
        e2.code(env);
        this.returnValue = LLVMBuildICmp( env.builder, LLVMIntSLT, e1.returnValue, e2.returnValue, "eq");
    }

}


/**
 * Defines AST constructor 'eq'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class eq extends Expression {
    protected Expression e1;
    protected Expression e2;

    /**
     * Creates "eq" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for e1
     * @param a2         initial value for e2
     */
    public eq(int lineNumber, Expression a1, Expression a2) {
        super(lineNumber);
        e1 = a1;
        e2 = a2;
    }

    public TreeNode copy() {
        return new eq(lineNumber, (Expression) e1.copy(), (Expression) e2.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "eq\n");
        e1.dump(out, n + 2);
        e2.dump(out, n + 2);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        this.compareSemant(e1, e2, env);
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_eq");
        e1.dump_with_types(out, n + 2);
        e2.dump_with_types(out, n + 2);
        dump_type(out, n);
    }

    @Override
    public void code(CodeGenEnv env) {
        e1.code(env);
        e2.code(env);
        this.returnValue = LLVMBuildICmp( env.builder, LLVMIntEQ, e1.returnValue, e2.returnValue, "eq");
    }

}


/**
 * Defines AST constructor 'leq'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class leq extends Expression {
    protected Expression e1;
    protected Expression e2;

    /**
     * Creates "leq" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for e1
     * @param a2         initial value for e2
     */
    public leq(int lineNumber, Expression a1, Expression a2) {
        super(lineNumber);
        e1 = a1;
        e2 = a2;
    }

    public TreeNode copy() {
        return new leq(lineNumber, (Expression) e1.copy(), (Expression) e2.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "leq\n");
        e1.dump(out, n + 2);
        e2.dump(out, n + 2);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        this.compareSemant(e1, e2, env);
    }

    @Override
    public void code(CodeGenEnv env) {
        e1.code(env);
        e2.code(env);
        this.returnValue = LLVMBuildICmp( env.builder, LLVMIntSLE, e1.returnValue, e2.returnValue, "eq");
    }

    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_leq");
        e1.dump_with_types(out, n + 2);
        e2.dump_with_types(out, n + 2);
        dump_type(out, n);
    }

}


/**
 * Defines AST constructor 'comp'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class comp extends Expression {
    protected Expression e1;

    /**
     * Creates "comp" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for e1
     */
    public comp(int lineNumber, Expression a1) {
        super(lineNumber);
        e1 = a1;
    }

    public TreeNode copy() {
        return new comp(lineNumber, (Expression) e1.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "comp\n");
        e1.dump(out, n + 2);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        e1.semant(env);
        if (e1.get_type() != AbstractTable.BooleanTypeSymbol){
            throw new SemanticException("Expect boolean expression here", this);
        }
        this.set_type(AbstractTable.BooleanTypeSymbol);
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_comp");
        e1.dump_with_types(out, n + 2);
        dump_type(out, n);
    }

}


/**
 * Defines AST constructor 'int_const'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class int_const extends Expression {
    protected AbstractSymbol token;

    /**
     * Creates "int_const" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for token
     */
    public int_const(int lineNumber, AbstractSymbol a1) {
        super(lineNumber);
        token = a1;
    }

    public TreeNode copy() {
        return new int_const(lineNumber, copy_AbstractSymbol(token));
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "int_const\n");
        dump_AbstractSymbol(out, n + 2, token);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        this.set_type( AbstractTable.IntTypeSymbol );
    }

    @Override
    public void code(CodeGenEnv env) {
        var v = Integer.parseInt(this.token.str);
        this.returnValue = LLVMConstInt( LLVMInt32Type(), v, 1);
    }

    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_int");
        dump_AbstractSymbol(out, n + 2, token);
        dump_type(out, n);
    }

}


/**
 * Defines AST constructor 'bool_const'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class bool_const extends Expression {
    protected Boolean val;

    /**
     * Creates "bool_const" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for val
     */
    public bool_const(int lineNumber, Boolean a1) {
        super(lineNumber);
        val = a1;
    }

    public TreeNode copy() {
        return new bool_const(lineNumber, copy_Boolean(val));
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "bool_const\n");
        dump_Boolean(out, n + 2, val);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        this.set_type( AbstractTable.BooleanTypeSymbol );

    }

    @Override
    public void code(CodeGenEnv env) {
        if (val)
            this.returnValue = LLVMConstInt(env.bool_type, 1, 0);
        else
            this.returnValue = LLVMConstInt(env.bool_type, 0, 0);
    }

    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_bool");
        dump_Boolean(out, n + 2, val);
        dump_type(out, n);
    }

}


/**
 * Defines AST constructor 'string_const'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class string_const extends Expression {
    protected AbstractSymbol token;

    /**
     * Creates "string_const" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for token
     */
    public string_const(int lineNumber, AbstractSymbol a1) {
        super(lineNumber);
        token = a1;
    }

    public TreeNode copy() {
        return new string_const(lineNumber, copy_AbstractSymbol(token));
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "string_const\n");
        dump_AbstractSymbol(out, n + 2, token);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        this.set_type( AbstractTable.StringTypeSymbol );
    }

    @Override
    public void code(CodeGenEnv env) {
        var s = this.token.str;
        var temp = env.global_text(s, s);
        var indices = new PointerPointer(env.const0_64, env.const0_64); //64 bit ints
        LLVMValueRef converted_p = LLVMConstInBoundsGEP(temp, indices, 2); //TODO
        this.returnValue = converted_p;
    }

    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_string");
        out.print(Utilities.pad(n + 2) + "\"");
        Utilities.printEscapedString(out, token.getString());
        out.println("\"");
        dump_type(out, n);
    }

}


/**
 * Defines AST constructor 'new_'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class new_ extends Expression {
    protected AbstractSymbol type_name;

    /**
     * Creates "new_" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for type_name
     */
    public new_(int lineNumber, AbstractSymbol a1) {
        super(lineNumber);
        type_name = a1;
    }

    public TreeNode copy() {
        return new new_(lineNumber, copy_AbstractSymbol(type_name));
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "new_\n");
        dump_AbstractSymbol(out, n + 2, type_name);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        //TODO check existance of t
        //TODO support self_type
        this.set_type(type_name);
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_new");
        dump_AbstractSymbol(out, n + 2, type_name);
        dump_type(out, n);
    }

}


/**
 * Defines AST constructor 'isvoid'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class isvoid extends Expression {
    protected Expression e1;

    /**
     * Creates "isvoid" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for e1
     */
    public isvoid(int lineNumber, Expression a1) {
        super(lineNumber);
        e1 = a1;
    }

    public TreeNode copy() {
        return new isvoid(lineNumber, (Expression) e1.copy());
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "isvoid\n");
        e1.dump(out, n + 2);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        e1.semant(env);
        this.set_type( AbstractTable.BooleanTypeSymbol );
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_isvoid");
        e1.dump_with_types(out, n + 2);
        dump_type(out, n);
    }

}


/**
 * Defines AST constructor 'no_expr'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class no_expr extends Expression {
    /**
     * Creates "no_expr" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     */
    public no_expr(int lineNumber) {
        super(lineNumber);
    }

    public TreeNode copy() {
        return new no_expr(lineNumber);
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "no_expr\n");
    }


    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_no_expr");
        dump_type(out, n);
    }

}


/**
 * Defines AST constructor 'object'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
class object extends Expression {
    protected AbstractSymbol name;

    /**
     * Creates "object" AST node.
     *
     * @param lineNumber the line in the source file from which this node came.
     * @param a1         initial value for name
     */
    public object(int lineNumber, AbstractSymbol a1) {
        super(lineNumber);
        name = a1;
    }

    public TreeNode copy() {
        return new object(lineNumber, copy_AbstractSymbol(name));
    }

    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "object\n");
        dump_AbstractSymbol(out, n + 2, name);
    }

    @Override
    public void semant(TypeCheckEnv env) {
        var t = (AbstractSymbol)env.symTable.lookup( name );
        if (this.name == TreeConstants.self){
            assert t == null;
            t = TreeConstants.SELF_TYPE;
        }
        else if (t == null) {
            // find parent class
            var parentFeature = env.findClassAttr(env.currentClass, name);
            if (parentFeature == null)
                throw new UndefinedSymbolException(name, this);
            t = parentFeature.type_decl;
        }
        this.set_type(t);
    }

    @Override
    public void code(CodeGenEnv env) {
        // TODO look up table in env, find address of symbol, then emit load return reg
        this.returnValue = env.valueMap.get( this.name.str ).code(env);
    }

    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_object");
        dump_AbstractSymbol(out, n + 2, name);
        dump_type(out, n);
    }

}


