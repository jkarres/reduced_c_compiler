import java.util.Vector;

public class StructType extends Type implements STO {
    //private Vector<NameAndType> members;
    private StructScope members;
    private String name;
    private boolean isThis = false;

    // the type from which this was created
    private IncompleteStructType ist;

    public StructType() {
        isThis = true;
    }

    //public StructType(String name, Vector<NameAndType> members) {
    public StructType(String name, StructScope members) {
        this.name = name;
        this.members = members;
        isThis = false;
        this.ist = members.getType();
    }

    public String getName() {
        return name;
    }

    /// \todo An equivalent-looking struct defined in another scope
    /// should *not* be equal to this, right?
    public boolean isExactlyEquivalent(Type t) {
        return t == this || t == ist;
    }

    /// \todo fix this
    public int size() {
        return members.size();
    }

    public String toString() {
        String retval = "<StructType>" +
            "<name>" + name + "</name>" +
            "<members>";
//         for (NameAndType nat : members)
//             retval += nat.toString + ", ";
        retval += members.toString();
        retval += "</members>";
        retval += "</StructType>";
        retval += "<isThis>" + isThis + "</isThis>";
        return retval;
    }

    // untypedef
    // since structtype sameness is by object identity only, we don't bother
    // untypedef'ing its components.

    public ObjectValue makeValueFrom(ObjectValue v) {
        // we don't deal with struct values at compile time, so this
        // shouldn't happen.
        assert false;
        return null;
    }

    public StructScope getMembers() {
        return members;
    }

}
