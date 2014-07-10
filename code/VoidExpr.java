class VoidExpr implements Expr {
    public Type getType() { return new VoidType(); }
    public Type getEquivType() { return new VoidType(); }

    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {

    }

    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) {

    }


}