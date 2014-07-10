public class VoidType extends Type {

    public String getName() {
        return "void";
    }

    public boolean isExactlyEquivalent(Type t) {
        return false;
    }

    public String toString() {
        return "<VoidType/>";
    }

    public VoidType untypedef() { return this; }

    public ObjectValue makeValueFrom(ObjectValue v) { 
        assert false;
        return null;
    }

    public int size() { return 4; }

}
