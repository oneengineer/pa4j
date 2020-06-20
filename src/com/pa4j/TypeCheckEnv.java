package com.pa4j;

import java.util.*;


public class TypeCheckEnv {

    public SymbolTable symTable;
    public ClassTable classTable;

    // should have store here too
    public class_c currentClass = null;
    public Stack<AbstractSymbol> callerStack; // symTable is for scope, this is for calling stack

    public int parseStage = 0;
    public boolean basicStage = false;

    // 0: only parse class names
    // 1: only parse attributes, method only parse parameter types
    // 2: parse body of methods

    public void enterCall(AbstractSymbol variableType){
        callerStack.push(variableType);
    }

    public AbstractSymbol currentCaller(){
        return callerStack.peek();
    }

    public void exitCall(){
        callerStack.pop();
    }

    protected HashMap<AbstractSymbol, class_c> symbol_class = new HashMap<>();

    public void addClass(AbstractSymbol name, class_c class_){
//        if (symbol_class.containsKey(name)){
//            throw new RuntimeException("Duplicated class" + name.getString());
//        }
        symbol_class.put(name, class_);
    }

    public class_c findClass(AbstractSymbol name){
        //TODO
        return symbol_class.get( name );
    }

    /**
     * check type that right (sub class) <= left (super class)
     * @param left
     * @param right
     * @return
     */
    public boolean checkAssignType(AbstractSymbol left, AbstractSymbol right){
        if (left == right) return true;
        if (right == TreeConstants.SELF_TYPE){
            // set right to the current class
            var t2 = this.currentClass.name;
            return checkAssignType(left, t2);
        }
        // left be self type, right must be

        if (right == TreeConstants.No_class) return false;
        var c = findClass(right);
        return checkAssignType( left, c.parent );
    }

    public attr findClassAttr(class_c class_, AbstractSymbol name){
        // In class_.features, element is either attr or method
        var iter = class_.features.getElements();
        while (iter.hasMoreElements()){
            var obj = iter.nextElement();
            if (obj instanceof attr){
                var t = (attr)obj;
                if (t.name == name)
                    return t;
            }
        }
        // find parent method
        if (class_.parent != TreeConstants.No_class){
            var p_class = findClass(class_.parent);
            if (p_class == null)
                throw new RuntimeException("parent class "+ class_.parent.getString() + " not exist");
            return findClassAttr(p_class, name);
        }
        return null;
    }

    public method findClassMethod(class_c class_, AbstractSymbol name){
        // In class_.features, element is either attr or method
        var iter = class_.features.getElements();
        while (iter.hasMoreElements()){
            var obj = iter.nextElement();
            if (obj instanceof method){
                var t = (method)obj;
                if (t.name == name)
                    return t;
            }
        }
        // find parent method
        if (class_.parent != TreeConstants.No_class){
            var p_class = findClass(class_.parent);
            if (p_class == null)
                throw new RuntimeException("parent class "+ class_.parent.getString() + " not exist");
            return findClassMethod(p_class, name);
        }
        return null;
    }

    public method findClassMethod(AbstractSymbol className, AbstractSymbol methodName){
        // In class_.features, element is either attr or method
        var class_ = this.findClass(className);
        if (class_ == null)
            throw new RuntimeException("Cannot find class " + className.getString());
        return findClassMethod(class_, methodName);
    }

    public static final HashSet<AbstractSymbol> basicClasses = new LinkedHashSet<>();

    static {
        basicClasses.add(TreeConstants.Object_);
        basicClasses.add(TreeConstants.IO);
        basicClasses.add(TreeConstants.Str);
        basicClasses.add(TreeConstants.Bool);
        basicClasses.add(TreeConstants.SELF_TYPE);
    }

    /**
     *
     * @param className
     * @return the chain of class names AbstractSymbol from Object ---> className
     */
    public List<AbstractSymbol> inheritChain(AbstractSymbol className ){
        class_c c_;
        // cannot override basic classes
        if (basicClasses.contains(className)){
            throw new RuntimeException("Cannot override basic class");
        }

        if (className == TreeConstants.SELF_TYPE){
            c_ = this.currentClass;
        }
        else c_ = this.findClass(className);

        if (c_ == null)
            throw new RuntimeException("Cannot find class" + className.getString());

        if (c_.parent != TreeConstants.No_class){ // No_class is the parent of Object
            var p = inheritChain(c_.parent);
            p.add(className);
            return p;
        }
        var l = new LinkedList<AbstractSymbol>();
        l.add(className);
        return l;
    }

    public AbstractSymbol joinedType(AbstractSymbol className1, AbstractSymbol className2){
        var chain1 = inheritChain(className1);
        var chain2 = inheritChain(className2);
        var i1 = chain1.iterator();
        var i2 = chain2.iterator();
        AbstractSymbol t1 = i1.next();
        if (t1 != i2.next())
            throw new RuntimeException("Strange all class should start from Object");
        while(i1.hasNext() && i2.hasNext()){
            var x1 = i1.next();
            var x2 = i2.next();
            if (x1 != x2)
                break;
            t1 = x1;
        }
        return t1;
    }
}
