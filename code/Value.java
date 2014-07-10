public abstract class Value {
    public abstract Type getType();
    public  Value getNegated() { return this; }
    public abstract String getAssembly();
}