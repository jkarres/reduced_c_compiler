public class FloatType extends NumericType {

    public String getName() {
        return "float";
    }

    public boolean isExactlyEquivalent(Type t) {
        return t instanceof FloatType;
    }

    public boolean isAssignableFrom(Type t) {
        return isExactlyEquivalent(t.untypedef()) ||
            t.untypedef() instanceof IntType;
    }

    public int size() {
        return 4;
    }

    public String toString() {
        return "<FloatType/>";
    }

    public FloatValue makeValueFrom(ObjectValue v) {
        return v.makeFloatValue();
    }
    
}