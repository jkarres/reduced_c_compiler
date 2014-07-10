class ArrayReference implements Expr, STO {
    private String name;
    private MemoryLocation memloc;
    private Type type;  // could be typedef

    // memloc is a location whose value is teh location of the array itself
    public ArrayReference(MemoryLocation memloc, String name, Type type) {
        this.memloc = memloc;
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Type getEquivType() {
        return type;
    }

    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) {
        aw.write("\t!\t" + "entering ArrayReference.putAddrInto " + offset);
        memloc.load(Register.L0, aw);
        aw.storeLocal(Register.L0, offset);
        aw.write("\t!\t" + "leaving ArrayReference.putAddrInto " + offset);
    }

    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {
        assert false;
    }

}
