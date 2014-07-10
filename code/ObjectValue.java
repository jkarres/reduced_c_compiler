public abstract class ObjectValue extends Value {
    public abstract Type getType();
    public abstract IntValue makeIntValue();
    public abstract FloatValue makeFloatValue();
    public abstract BooleanValue makeBooleanValue();
    public abstract CharValue makeCharValue();
}