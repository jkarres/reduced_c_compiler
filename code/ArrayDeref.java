class ArrayDeref implements MLV {

    private Expr base;

    private Expr offsetExpr;
    private int scale;
    private Type returnType;

    static private SymbolTable symbolTable;

    static public void setSymbolTable(SymbolTable st) {
        symbolTable = st;
    }

    public ArrayDeref(Expr base, Expr offset) {
        this.base = base;
        this.offsetExpr = offset;

        Type tempType = base.getType().untypedef();
        if (tempType instanceof ArrayType) {

            ArrayType at = (ArrayType)tempType;
            returnType = at.getBaseType();

        } else if (tempType instanceof PointerType) {

            PointerType pt = (PointerType)tempType;
            returnType = (Type)pt.getPointee();

        }

        scale = ((Type)returnType).size();

    }

    public Type getType() {
        return returnType;
    }

    public Type getEquivType() {
        return returnType.untypedef();
    }


    /// it is a mistake to use this if the resulting object does not
    /// fit into 4 bytes.
    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {

        aw.write("\t!\t" + "entering ArrayDeref.putValueInto");

        // put offset count into %fp - baseAddrTemp
        int offsetCountTemp = symbolTable.getTemp(4);
        offsetExpr.putValueInto(offsetCountTemp, aw, st);

        // put base address into L0
        if (base.getType().untypedef() instanceof PointerType) {
            base.putValueInto(offset, aw, st);
        } else if (base.getType().untypedef() instanceof ArrayType) {
            base.putAddrInto(offset, aw, st);
        } else {
            aw.write("Not good!");
        }
        aw.loadLocal(offset, Register.L0);


        // put offset count into O0
        aw.loadLocal(offsetCountTemp, Register.O0);


        // put scale into O1
        aw.write("\t" + "set\t" + scale + ", " + Register.O1);

        // multiply O0 (offset count) and O1 (scale) yielding O0
        // (offset in bytes)
        aw.write("\t" + "call\t" + ".mul");
        aw.write("\t" + "nop");

        // add O0 (offset in bytes) and L0 (base address) and save to L0
        aw.write("\t" + "add\t" + Register.O0 + ", " + Register.L0 + ", " + Register.L0);

        // load [L0] (addr of rval) into L0 (rval)
        aw.write("\t" + "ld\t" + "[" + Register.L0 + "], " + Register.L0);

        // write L0 to [%fp - offset]
        aw.storeLocal(Register.L0, offset);

        aw.write("\t!\t" + "leaving ArrayDeref.putValueInto");

    }

    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) {

        aw.write("\t!\t" + "entering ArrayDeref.putAddrInto");

        // put offset count into %fp - offsetCountTemp
        int offsetCountTemp = symbolTable.getTemp(4);
        aw.write("\t!\t" + "putting offset value into " + offsetCountTemp);
        offsetExpr.putValueInto(offsetCountTemp, aw, st);

        // put base address into L0
        aw.write("\t!\t" + "Now to put the base address into %l0, by way of " + offset);
        aw.write("\t!\t" + "base is " + base.toString());
        if (base.getType().untypedef() instanceof PointerType) {
            aw.write("\t!\t" + "which is PointerType");
            base.putValueInto(offset, aw, st);
        } else if (base.getType().untypedef() instanceof ArrayType) {
            aw.write("\t!\t" + "which is ArrayType");
            base.putAddrInto(offset, aw, st);
        } else {
            aw.write("Not good!");
        }
        aw.loadLocal(offset, Register.L0);

        // put offset count into O0
        aw.loadLocal(offsetCountTemp, Register.O0);

        // put scale into O1
        aw.write("\t" + "set\t" + scale + ", " + Register.O1);

        // multiply O0 (offset count) and O1 (scale) yielding O0
        // (offset in bytes)
        aw.write("\t" + "call\t" + ".mul");
        aw.write("\t" + "nop");

        // add O0 (offset in bytes) and L0 (base address) and save to
        // L0 (an addr)
        aw.write("\t" + "add\t" + Register.O0 + ", " + Register.L0 + ", " + Register.L0);

        // write L0 to [%fp - offset]
        aw.storeLocal(Register.L0, offset);


        aw.write("\t!\t" + "leaving ArrayDeref.putAddrInto");

    }
}
