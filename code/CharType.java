public class CharType extends Type {

    public String getName() {
        return "char";
    }

    public boolean isExactlyEquivalent(Type t) {
        return t instanceof CharType;
    }

    public int size() {
        return 1;
    }

    public String toString() {
        return "<CharType/>";
    }

    public CharValue makeValueFrom(ObjectValue v) {
        return v.makeCharValue();
    }
}