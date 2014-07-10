public class TypedefType extends Type implements STO {
    private String name;
    private Type realType;

    public TypedefType(String name, Type realType) {
        this.name = name;
        this.realType = realType;
    }

    public Type getRealType() {
        return realType;
    }

    public String getName() {
        return name;
    }

    public boolean isExactlyEquivalent(Type t) {
        if (t instanceof TypedefType) {
            TypedefType tt = (TypedefType)t;
            return this.name.equals(tt.name) && 
                this.realType.isExactlyEquivalent(tt.realType);
        } else {
            return false;
        }
    }

    public boolean isAssignableFrom(Type t) {
        return untypedef().isAssignableFrom(t);
    }

    public int size() {
        return realType.size();
    }

    public String toString() {
        String nameString =
            name == null ? "null" : name;
        String realTypeString =
            realType == null ? "null" : realType.toString();

        return "<TypedefType>" +
            "<name>" + nameString + "</name>" +
            "<realType>" + realTypeString + "</realType>" +
            "</TypedefType>";
    }

    public Type untypedef() {
        return realType.untypedef();
    }
    
    public Type topUntypedef() {
        return realType.topUntypedef();
    }

    public ObjectValue makeValueFrom(ObjectValue v) {
        return realType.makeValueFrom(v);
    }

    public boolean hasBareIST() { return realType.hasBareIST(); }

}
