class BoundStructFunc implements Expr {
    Expr thisExpr;
    Expr func;

    /// unmangled
    String funcName;

    String structName;

    BoundStructFunc(Expr thisExpr, Expr func) {
        this.thisExpr = thisExpr;
        this.func = func;
        this.funcName = ((STO)func).getName();

        if (thisExpr.getType().untypedef() instanceof StructType) {
            this.structName = 
                ((StructType)thisExpr.getType().untypedef()).getName();
        } else if (thisExpr.getType().untypedef() instanceof IncompleteStructType) {
            this.structName = 
                ((IncompleteStructType)thisExpr.getType().untypedef()).getName();
        } else {
            assert false;
        }

    }

    public Type getType() { 
        return func.getType();
    }

    public Type getEquivType() { assert false; return null; }
    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) { assert false; }
    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) { assert false; }

    public String getFuncName() {
        return funcName;
    }

    public String getStructName() {
        return structName;
    }

    public Expr getThis() { return thisExpr; }

}
