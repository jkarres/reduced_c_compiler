public abstract class Type implements TypeOrReportedErr, TypeOrErr {

    public abstract String getName();

    public abstract boolean isExactlyEquivalent(Type t);

    public boolean isTypedefEquivalent(Type t) {
        return untypedef().isExactlyEquivalent(t.untypedef());
    }

    public boolean isAssignableFrom(Type t) {
        //System.out.println("in isAssignableFrom.");
        //System.out.println("this: " + this);
        //System.out.println("t: " + t);
        boolean retval = untypedef().isExactlyEquivalent(t.untypedef());
        //System.out.println("returning " + retval);
        return retval;
    }

    /// Recursively untypedef everything in this type.
    public Type untypedef() {
        return this;
    }

    /// untypedef until you hit the first non-typedef thing.  The
    /// difference between this and untypedef() is that untypedef()
    /// will remove typedef types from structure members,
    /// pointer/array basetypes, etc., and this won't.
    public Type topUntypedef() {
        return this;
    }

    public boolean hasBareIST() { return false; }


    public abstract int size();
    public abstract ObjectValue makeValueFrom(ObjectValue v);

}
