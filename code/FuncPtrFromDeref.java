import java.util.Vector;

class FuncPtrFromDeref extends AbstractFunctionPointer {
    private Expr ptr;

    public FuncPtrFromDeref(FunctionType ft, Expr ptr) {
        super(ft);
        this.ptr = ptr;
    }

    protected void putAddrInto(Register r, AssemblyWriter aw, SymbolTable symtab) {
        aw.write("\t!\t" + "Entering FuncPtrFromDeref.putAddrInto(" + r + " ,...)");
        int addrTemp = symtab.getTemp(4);
        ptr.putValueInto(addrTemp, aw, symtab);
        aw.loadLocal(addrTemp, r);
        aw.write("\t!\t" + "Leaving FuncPtrFromDeref.putAddrInto(" + r + " ,...)");
    }

    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) {
        aw.write("\t!\t" + "Entering FuncPtrFromDeref.putAddrInto(" + offset + " ,...)");
        ptr.putValueInto(offset,  aw, st);
        aw.write("\t!\t" + "Leaving FuncPtrFromDeref.putAddrInto(" + offset + " ,...)");
    }

    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {
        assert false;
    }


}

