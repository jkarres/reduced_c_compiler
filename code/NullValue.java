public class NullValue extends ObjectValue {
    public Type getType() {
        return new NullType();
    }

    public IntValue makeIntValue() {
        return new IntValue(0);
    }

    public FloatValue makeFloatValue() {
        return new FloatValue((float)0.0);
    }

    public BooleanValue makeBooleanValue() {
        return new BooleanValue(false);
    }

    public CharValue makeCharValue() {
        assert false;
        return null;
    }

    public String getAssembly() {
        return "0";
    }

}