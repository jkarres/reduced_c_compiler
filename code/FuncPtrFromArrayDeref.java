class FuncPtrFromArrayDeref extends AbstractFunctionPointer {
    private Expr base;
    private Expr index;

    public FuncPtrFromArrayDeref(FunctionType ft, Expr base, Expr index) {
        super(ft);
        this.base = base;
        this.index = index;
    }

    protected void putAddrInto(Register r, AssemblyWriter aw, SymbolTable symtab) {
        aw.write("\t!\t" + "Entering FuncPtrFromArrayDeref.putAddrInto(" + r + " ,...)");

        int baseAddrTemp = symtab.getTemp(4);
        base.putAddrInto(baseAddrTemp, aw, symtab);

        int indexValueTemp = symtab.getTemp(4);
        index.putValueInto(indexValueTemp, aw, symtab);

        // get offset byte count
        aw.write("\t" + "set\t" + type.size() + ", " + Register.O0);
        aw.loadLocal(indexValueTemp, Register.O1);
        aw.write("\t" + "call\t" + ".mul");
        aw.write("\t" + "nop");

        // offset byte count now in %o0

        aw.loadLocal(baseAddrTemp, Register.O1);
        aw.write("\t" + "add\t" + Register.O0 + ", " + Register.O1 + ", " + r);

        // the desired location now in r

        aw.write("\t!\t" + "Leaving FuncPtrFromArrayDeref.putAddrInto(" + r + " ,...)");
    }

    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable symtab) {
        aw.write("\t!\t" + "Entering FuncPtrFromArrayDeref.putAddrInto(" + offset + " ,...)");

        int baseAddrTemp = symtab.getTemp(4);
        base.putAddrInto(baseAddrTemp, aw, symtab);

        int indexValueTemp = symtab.getTemp(4);
        index.putValueInto(indexValueTemp, aw, symtab);

        // get offset byte count
        aw.write("\t" + "set\t" + type.size() + ", " + Register.O0);
        aw.loadLocal(indexValueTemp, Register.O1);
        aw.write("\t" + "call\t" + ".mul");
        aw.write("\t" + "nop");

        // offset byte count now in %o0

        aw.loadLocal(baseAddrTemp, Register.O1);
        aw.write("\t" + "add\t" + Register.O0 + ", " + Register.O1 + ", " + Register.O0);

        // the desired location now in r
        aw.storeLocal(Register.O0, offset);

        aw.write("\t!\t" + "Leaving FuncPtrFromArrayDeref.putAddrInto(" + offset + " ,...)");
    }

    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {
        assert false;
    }

    


}
