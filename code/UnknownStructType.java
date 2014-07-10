public class UnknownStructType extends StructType {

    public String getName() {
        return "UnknownStructType";
    }

    public boolean isExactlyEquivalent(Type t) {
        return false;
    }

    public String toString() {
        return "<UnknownStructType/>";
    }
}