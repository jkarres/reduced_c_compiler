public class ArrayType extends Type {
    private int numElements;
    private Type baseType;

    public ArrayType(int numElements, Type baseType) {
        this.numElements = numElements;
        this.baseType = baseType;
    }

    public String getName() {
        return baseType.getName() + "[" + numElements + "]";
    }

    public int getNumElements() {
        return numElements;
    }

    public Type getBaseType() {
        return baseType;
    }

    public boolean isExactlyEquivalent(Type t) {
        if (t instanceof ArrayType) {
            ArrayType at = (ArrayType)t;
            return this.numElements == at.numElements && baseType.isExactlyEquivalent(at.baseType);
        } else {
            return false;
        }
    }

    public int size() {
        return numElements * baseType.size();
    }

    public Type untypedef() {
        return new ArrayType(numElements, baseType.untypedef());
    }

    public String toString() {
        return "<ArrayType>" +
            "<numElements>" + numElements + "</numElements>" +
            "<baseType>" + baseType.toString() + "</baseType>" +
            "</ArrayType>";
    }

    public ObjectValue makeValueFrom(ObjectValue v) {
        // objects of array type are not assignable, so this should
        // never be called.
        assert false;
        return null;
    }

    public boolean hasBareIST() {
        return baseType.hasBareIST();
    }

}
