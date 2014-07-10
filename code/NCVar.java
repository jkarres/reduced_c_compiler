public class NCVar implements STO, MLV, HasLocation {
    private final Type type;
    private final String name;
    private MemoryLocation memloc;

    public NCVar(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public Type getEquivType() {
        return type.untypedef();
    }

    public String getName() {
        return name;
    }

    public void setLocation(MemoryLocation ml) {
        memloc = ml;
    }

    public MemoryLocation getLocation() {
        return memloc;
    }

    public int getSize() { 
        return type.size();
    }

    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {
        aw.write("\t!\t" + "putting value of " + name + " into %fp - " + offset);
        memloc.load(Register.G1, aw);
        aw.storeLocal(Register.G1, offset);
        aw.write("");
    }

    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) {
        aw.write("\t!\t" + "putting address of " + name + " into %fp - " + offset);
        memloc.putAddress(Register.G1, aw);

        aw.storeLocal(Register.G1, offset);

        aw.write("");
    }

    public String toString() {
        return "<NCVar>" +
            "<type>" + type.toString() + "</type>" +
            "<name>" + name + "</name>" +
            "</NCVar>";
    }
}
