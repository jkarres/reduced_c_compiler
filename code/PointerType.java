public class PointerType extends Type {
    private Type pointee;

    public PointerType(Type pointee) {
        this.pointee = pointee;
    }

    public String getName() {
        String pointeeName = 
            pointee == null ? "null" : pointee.getName();
        if (pointee.topUntypedef() instanceof FunctionType)
            return pointeeName;
        else
            return pointeeName + "*";
    }

    public Type getBaseType() {
        return pointee;
    }

    public boolean isExactlyEquivalent(Type t) {
        if (t instanceof PointerType) {
            PointerType pt = (PointerType)t;
            return this.pointee.isExactlyEquivalent(pt.pointee);
        } else {
            return false;
        }
    }

    public int size() {
        return 4;
    }

    public String toString() {
        String pointeeString =
            pointee == null ? "null" : pointee.toString();
        return "<PointerType>" +
            "<pointee>" + pointeeString + "</pointee>" +
            "</PointerType>";
    }

    public Type getPointee() {
        return pointee;
    }

    public Type untypedef() {
        return new PointerType(pointee.untypedef());
    }

    public ObjectValue makeValueFrom(ObjectValue v) {
        if (v instanceof NullValue) {
            return new PointerValue(this, 0);
        } else if (v instanceof IntValue) {
            return new PointerValue(this, ((IntValue)v).getValue());
        } else if (v instanceof FloatValue) {
            return new PointerValue(this, (int)((FloatValue)v).getValue());
        } else if (v instanceof BooleanValue) {
            if (((BooleanValue)v).getValue()) {
                return new PointerValue(this, 1);
            } else {
                return new PointerValue(this, 0);
            }
        } else if (v instanceof PointerValue) {
            return new PointerValue(this, ((PointerValue)v).getValue());
        }
        
        // we don't deal with pointer values at compile time
        assert false;
        return null;
    }

    public boolean isAssignableFrom(Type t) {
        if (super.isAssignableFrom(t)) {
            return true;
        }

        Type rt = t.topUntypedef();
        if (rt instanceof ArrayType) {
            return pointee.isTypedefEquivalent(((ArrayType)rt).getBaseType());
        } else if (rt instanceof NullType) {
            return true;
        } else if (pointee.topUntypedef() instanceof FunctionType) {
            return pointee.isAssignableFrom(rt);
        } else {
            return false;
        }
    }

    public boolean hasBareIST() {
        if (pointee instanceof IncompleteStructType) {
            return false;
        } else {
            return pointee.hasBareIST();
        }
    }
}
