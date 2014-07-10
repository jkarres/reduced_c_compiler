public class CVar implements ConstExpr, NMLV, STO, HasLocation {
    private final Value value;
    private final String name;
    private final Type type;
    private MemoryLocation memloc;

    public CVar(Value value, String name) {
        this.value = value;
        this.name = name;
        this.type = value.getType();
    }

    public CVar(Value value, String name, Type type) {
        this.value = value;
        this.name = name;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Type getEquivType() {
        return value.getType().untypedef();
    }

    public Value getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        String retval =  "<CVar>";
        retval += "<value>" + value.toString() + "</value>";
        retval += "<name>" + name + "</name>";
        retval += "</CVar>";
        return retval;
    }

    public ConstExpr getNegated() {
        Value negated = value.getNegated();
        if (negated instanceof ObjectValue) {
            return new CRV((ObjectValue)negated);
        } else {
            return this;
        }
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

    public int getSize() {
        if (type instanceof Type)
            return ((Type)type).size();
        else
            return 0;
    }

    public void setLocation(MemoryLocation ml) {
        memloc = ml;
    }

    public MemoryLocation getLocation() {
        return memloc;
    }



}
