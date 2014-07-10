class FuncPtrFromStruct extends AbstractFunctionPointer {
    private Expr base;
    private int offset;

    public FuncPtrFromStruct(FunctionType ft, Expr base, int offset) {
        super(ft);
        this.base = base;
        this.offset = offset;
    }

    protected void putAddrInto(Register r, AssemblyWriter aw, SymbolTable symtab) {
        aw.write("\t!\t" + "Entering FuncPtrFromStruct.putAddrInto(" + r + " ,...)");

        int baseAddrTemp = symtab.getTemp(4);
        base.putAddrInto(baseAddrTemp, aw, symtab);

        aw.write("\t" + "set\t" + this.offset + ", " + Register.O0);
        aw.loadLocal(baseAddrTemp, Register.O1);
        aw.write("\t" + "add\t" + Register.O0 + ", " + Register.O1 + ", " + r);

        // the desired location now in r
        aw.write("\t!\t" + "Leaving FuncPtrFromStruct.putAddrInto(" + r + " ,...)");
    }

    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable symtab) {
        aw.write("\t!\t" + "Entering FuncPtrFromStruct.putAddrInto(" + offset + " ,...)");

        int baseAddrTemp = symtab.getTemp(4);
        base.putAddrInto(baseAddrTemp, aw, symtab);

        aw.write("\t" + "set\t" + this.offset + ", " + Register.O0);
        aw.loadLocal(baseAddrTemp, Register.O1);
        aw.write("\t" + "add\t" + Register.O0 + ", " + Register.O1 + ", " + Register.O0);

        aw.storeLocal(Register.O0, offset);

        aw.write("\t!\t" + "Leaving FuncPtrFromStruct.putAddrInto(" + offset + " ,...)");
    }

    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {
        assert false;
    }
    
}
