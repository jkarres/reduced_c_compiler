public class NCRV implements RV {
    private final Type type;

    /// the temp location where this value lives
    private int location = 0;
    private boolean locationSet = false;

    public NCRV(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Type getEquivType() {
        return type.untypedef();
    }

    public String toString() {
        return "<NCRV>" +
            "<type>" + type.toString() + "</type>" +
            "</NCRV>";
    }

    /// emit code to copy this value into offset
    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {
        assert locationSet;
        aw.loadLocal(location, Register.G1);
        aw.storeLocal(Register.G1, offset);
        aw.write("");

    }

    // had to quit asserting false since we could have a function that
    // returns a (temp) struct and then call a function belonging to that struct.
    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) {
        //System.out.println(this);
        //assert false;
        aw.write("\t" + "sub\t" + "%fp," + location + ", " + Register.G1);
        aw.storeLocal(Register.G1, offset);
        
    }

    public void setLocation(int l) {
        if (locationSet) assert false;
        location = l;
        locationSet = true;
    }
}
