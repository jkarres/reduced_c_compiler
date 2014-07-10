import java.util.Vector;

// A function pointer object takes up 3 words.  The first word is a
// pointer to the function.  The second word is either 1 or 0,
// indicating whether there is an implicit first argument.  The third
// argument is a pointer to the struct that is the implicit argument,
// if there is one.

class FunctionPointer extends AbstractFunctionPointer implements STO, HasLocation {
    private String name;

    private MemoryLocation memloc;

    public FunctionPointer(FunctionType type, String name) {
        super(type);

        this.name = name;

    }

    public String getName() {
        return name;
    }

    protected void putAddrInto(Register r, AssemblyWriter aw, SymbolTable symtab) {
        memloc.putAddress(r, aw);
    }


    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) {
        aw.write("\t!\t" + "putting address of " + name + " into %fp - " + offset);
        memloc.putAddress(Register.G1, aw);
        aw.storeLocal(Register.G1, offset);
        aw.write("");
    }

    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {
        assert false;
    }

    public void setLocation(MemoryLocation loc) {
        this.memloc = loc;
    }

    public MemoryLocation getLocation() {
        return memloc;
    }

    public int getSize() {
        return 12;
    }


}
