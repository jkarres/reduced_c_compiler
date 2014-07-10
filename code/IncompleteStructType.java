public class IncompleteStructType extends Type implements STO {

    private String name;
    private StructScope members;

    public IncompleteStructType(String name, StructScope members) {
        this.name = name;
        this.members = members;
    }

    public int size() {
        assert false;
        return 0;
    }

    public ObjectValue makeValueFrom(ObjectValue v) {
        assert false;
        return null;
    }

    public String getName() {
        return name;
    }

    public boolean isExactlyEquivalent(Type t) {
        if (t == this) {
            return true;
        }

        Type realType = t.untypedef();

        if (realType instanceof StructType) {
            return realType.isExactlyEquivalent(this);
        }

        return false;
    }

    public StructScope getMembers() {
        return members;
    }

    public boolean hasBareIST() { return true; }

}