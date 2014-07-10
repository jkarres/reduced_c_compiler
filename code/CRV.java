public class CRV implements RV, ConstExpr {
    private final ObjectValue value;
    private final Type type;

    public CRV(ObjectValue value) {
        this.value = value;
        this.type = value.getType();
    }

    public CRV(ObjectValue value, Type type) {
        this.value = value;
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

    public String toString() {
        return "<CRV>" + 
            "<value>" + value.toString() + "</value>" +
            "</CRV>";
    }

    public CRV getNegated() {
        Value negated = value.getNegated();
        if (negated instanceof ObjectValue) {
            return new CRV((ObjectValue)negated);
        } else {
            return this;
        }
    }

    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {
        aw.write("\t!\t" + "putting const value into %fp - " + offset);
        aw.write("\t" + "set" + "\t" + value.getAssembly() + ", " + Register.G1);
        aw.storeLocal(Register.G1, offset);
        aw.write("");
    }

    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) {
        assert false;
    }

}