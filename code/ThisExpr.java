class ThisExpr implements NMLV {
    private IncompleteStructType type;
    private ArgumentMemoryLocation location;

    public ThisExpr(IncompleteStructType type) {
        this.type = type;
        this.location = new ArgumentMemoryLocation(0);
    }

    public IncompleteStructType getType() {
        return type;
    }

    // no changes, since structs use name equivalence
    public IncompleteStructType getEquivType() {
        return type;
    }

    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {
        assert false;
    }

    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) {
        aw.loadLocal(-68, Register.L0);
        aw.storeLocal(Register.L0, offset);
    }

}