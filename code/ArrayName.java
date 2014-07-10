class ArrayName implements NMLV, STO, HasLocation {
    private final String name;
    // not ArrayType since it could be a typedef type as well
    private final Type type;

    private MemoryLocation memloc;

    public void setLocation(MemoryLocation memloc) { this.memloc = memloc; }
    public MemoryLocation getLocation() { return memloc; }

    public int getSize() { return ((Type)type).size(); }

    public ArrayName(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public Type getType() { return type; }
    
    public Type getEquivType() { return type.untypedef(); }

    public String getName() { return name; }

    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {
        // trying this on the theory that giving it an address in
        // place of a value will effect automatic array -> ptr conversion
        putAddrInto(offset, aw, st);
    }

    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) {
        aw.write("\t!\t" + "putting address of " + name + " into %fp - " + offset);
        memloc.putAddress(Register.G1, aw);
        aw.storeLocal(Register.G1, offset);
        aw.write("");
    }

}
