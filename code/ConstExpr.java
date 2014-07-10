public interface ConstExpr extends Expr, ConstExprOrReportedErr, ConstExprOrUnreportedErr, ConstExprOrErr, ConstExprOrConstErrOrReportedErr {
    public Value getValue();
    public ConstExpr getNegated();
}