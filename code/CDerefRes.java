public class CDerefRes implements ConstExpr, NMLV {
    private final Value value;
    private final Type type;

    public CDerefRes(CVar cv) {
        this.value = cv.getValue();
        this.type = cv.getValue().getType();
    }

    public CDerefRes(Value value) {
        this.value = value;
        this.type = value.getType();
    }

    public CDerefRes(CVar cv, Type type) {
        this.value = cv.getValue();
        this.type = type;
    }

    public CDerefRes(Value value, Type type) {
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
        String retval =  "<CVar>";
        retval += "<value>" + value.toString() + "</value>";
        retval += "</CVar>";
        return retval;
    }

    public CDerefRes getNegated() {
        return new CDerefRes(value.getNegated());
    }

    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {

    }

    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) {

    }


}