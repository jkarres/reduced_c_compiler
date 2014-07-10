public class ConstErr extends UnreportedErr implements ConstExprOrConstErrOrReportedErr  {
    final public Type type;
    public ConstErr(Type t) {
        type = t;
    }
}