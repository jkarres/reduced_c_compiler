import java.util.Vector;

interface Callable extends Expr {
    public FunctionType getType();

    public Expr call(Vector<Expr> args, AssemblyWriter aw, SymbolTable symtab);

    public void assignInto(AbstractFunctionPointer lhs, AssemblyWriter aw, 
                           SymbolTable symtab);

}
