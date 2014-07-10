// this is a STO only so that we can put it in function scopes for params passed by ref
public class NCDerefRes implements MLV, STO {
    final private Type type;

    /// its value should be an address
    Expr operand;

    /// used only when acting as a STO
    String name; 

    public NCDerefRes(Type type) {
        this.type = type;
    }

    public NCDerefRes(NCVar nc) {
        this(nc.getType());
        this.name = nc.getName();
    }

    // only used when acting a a STO
    public String getName() {
        return name;
    }

    public String toString() {
        return "<NCDerefRes>" + 
            "<name>" + name + "</name>" +
            "<type>" + type.toString() + "</type>" +
            "<operand>" + operand.toString() + "</operand>" +
            "</NCDerefRes>";
    }

    public Type getType() {
        return type;
    }

    public Type getEquivType() {
        return type.untypedef();
    }
    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {
        if (operand == null) assert false;

        aw.write("\t!\t" + "entering NCDerefRes.putValueInto " + offset);

        operand.putValueInto(offset, aw, st);

        aw.loadLocal(offset, Register.G1);

        // G1 contains the address at which the rv is located
        // (i.e., the value of the pointer)

        // here is where we do the actual dereferencing, but first
        // we're going to check whether we've got a null pointer on
        // our hands.

        // all is well, back to our regularly scheduled dereferencing
        aw.write("\t" + "ld" + "\t" + "[" + Register.G1 + "], " + Register.G1);

        // G1 now contains the actual value

        aw.storeLocal(Register.G1, offset);
        aw.write("\t!\t" + "leaving NCDerefRes.putValueInto " + offset);

    }

    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) {

        aw.write("\t!\t" + "entering NCDerefRes.putAddrInto");

        operand.putValueInto(offset, aw, st);

        aw.write("\t!\t" + "leaving NCDerefRes.putAddrInto");
    }

    public void setOperand(Expr e) {
        //assert e.getEquivType() instanceof PointerType;
        operand = e;
    }


}
