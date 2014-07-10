public interface Expr extends ExprOrErr, ExprOrReportedErr, ExprOrNothingOrReportedErr {
    public Type getType();
    public Type getEquivType();
    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st);
    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st);
}
