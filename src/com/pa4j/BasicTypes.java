package com.pa4j;

class IntType extends AbstractSymbol{

    /**
     * Constructs a new table entry.
     *
     */
    public IntType() {
        super("Int", "Int".length(), 0);
    }

    // singleton
    private static IntType outInstance;

    static {
        outInstance = new IntType();
    }


    public static IntType getInstance() {
        return outInstance;
    }

    @Override
    public Object clone() {
        return this;
    }
}


class StringType extends AbstractSymbol{

    /**
     * Constructs a new table entry.
     *
     */
    public StringType() {
        super("String", "String".length(), 0);
    }

    // singleton
    private static StringType outInstance;

    static {
        outInstance = new StringType();
    }

    public static StringType getInstance() {
        return outInstance;
    }

    @Override
    public Object clone() {
        return this;
    }
}

