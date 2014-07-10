public class BooleanType extends Type {

    public String getName() {
        return "bool";
    }

    public boolean isExactlyEquivalent(Type t) {
        return t instanceof BooleanType;
    }

    public int size() {
        return 4;
    }

    public String toString() {
        return "<BooleanType/>";
    }

    public BooleanValue makeValueFrom(ObjectValue v) {
        return v.makeBooleanValue();
    }
}