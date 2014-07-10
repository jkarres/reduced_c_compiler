public class NullType extends Type {

    public String getName() {
        return "NULL";
    }

    public boolean isExactlyEquivalent(Type t) {
        return t instanceof NullType;
    }

    public int size() {
        return 4;
    }

    public String toString() {
        return "</NullType>";
    }

    public NullValue makeValueFrom(ObjectValue v) {
        return new NullValue();
    }

}