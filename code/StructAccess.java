class StructAccess implements MLV {
    Expr struct;
    Type type;
    int offsetInStruct;

    public String toString() {
        return "<StructAccess>" + 
            "<struct>" + struct.toString() + "</struct>" +
            "<type>" + type.toString() + "</type>" +
            "<offsetInStruct>" + offsetInStruct + "</offsetInStruct>" +
            "</StructAccess>";
    }

    public StructAccess(Expr struct, Type type, int offset) {
        this.struct = struct;
        this.type = type;
        this.offsetInStruct = offset;
    }

    public Type getType() { return type; }

    public Type getEquivType() { return type.untypedef(); }

    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {
        aw.writeComment("Entering StructAccess.putValueInto(" + offset + ")");

        struct.putAddrInto(offset, aw, st);

        aw.loadLocal(offset, Register.L0);

        aw.write("\t" + "add\t" + Register.L0 + ", " + offsetInStruct + ", " + Register.L0);
        aw.write("\t" + "ld\t" + "[" + Register.L0 + "], " + Register.L1);
        aw.storeLocal(Register.L1, offset);

        aw.writeComment("Leaving StructAccess.putValueInto");

    }

    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) {
        aw.writeComment("Entering StructAccess.putAddreInto(" + offset + ")");
        struct.putAddrInto(offset, aw, st);

        aw.loadLocal(offset, Register.L0);

        aw.write("\t" + "add\t" + Register.L0 + ", " + offsetInStruct + ", " + Register.L0);
        aw.storeLocal(Register.L0, offset);
        aw.writeComment("Leaving StructAccess.putAddreInto(" + offset + ")");
    }
}
