import java.util.Vector;

class UnboundFunction implements FunctionSTO, Expr {

    // the undecorated name
    private String name;

    // the type of this function
    private FunctionType type;
    
    // the decorated name (that incorporates the struct name)
    private String fullName;

    public UnboundFunction(String name, FunctionType type, String structName) {
        this.name = name;
        this.type = type;
        this.fullName = structName + "." + name;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public BoundFunction bind(Expr implicitArgument) {
        return new BoundFunction(fullName, type, implicitArgument);
    }

    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) {
        assert false;
    }

    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {
        assert false;
    }

    public FunctionType getType() { 
        return type;
    }

    public Type getEquivType() {
        return type.untypedef();
    }

}
