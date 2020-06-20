package com.pa4j;

class SemanticException extends RuntimeException {
    public TreeNode errorAst;
    public SemanticException(String message, TreeNode errorAst) {
        super(message);
        this.errorAst = errorAst;
    }
}


class UnMatchTypeException extends SemanticException {
    public UnMatchTypeException(AbstractSymbol left, AbstractSymbol right, TreeNode errorAst) {
        super(right.getString() + " cannot be assigned to " + left.getString(), errorAst);
    }
}

class UndefinedSymbolException extends SemanticException {
    public UndefinedSymbolException(AbstractSymbol name, TreeNode errorAst) {
        super("undefined symbol:" + name.getString(), errorAst);
    }
}

class MethodOverridingException extends SemanticException {
    public MethodOverridingException(AbstractSymbol methodName, String message, TreeNode errorAst) {
        super("method overriding error:" + methodName.getString() + " " + message, errorAst);
    }
}
