public class IntType extends NumericType {
    public String getName() { 
        return "int"; 
    }

    public boolean isExactlyEquivalent(Type t) {
        boolean rv = t instanceof IntType; 
        return rv;
    }

    public int size() {
        return 4;
    }

    public String toString() {
        return "<IntType/>";
    }

    public IntValue makeValueFrom(ObjectValue v) {
        return v.makeIntValue();
    }
}
