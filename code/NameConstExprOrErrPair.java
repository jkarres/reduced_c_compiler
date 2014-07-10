public class NameConstExprOrErrPair {
    public String name;
    public ConstExprOrConstErrOrReportedErr value;

    public NameConstExprOrErrPair(String name, ConstExprOrConstErrOrReportedErr value) {
        this.name = name;
        this.value = value;
    }
}
