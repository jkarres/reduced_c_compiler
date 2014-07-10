public class BooleanValue extends ObjectValue {
    private final boolean value;

    public BooleanValue(boolean b) {
        value = b;
    }

    public boolean getValue() {
        return value;
    }

    public Type getType() {
        return new BooleanType();
    }

    public String toString() {
        return "<BooleanValue>" + value + "</BooleanValue>";
    }

    public IntValue makeIntValue() {
        return new IntValue(value ? 1 : 0);
    }

    public FloatValue makeFloatValue() {
        return new FloatValue(value ? (float)1.0 : (float)0.0);
    }

    public BooleanValue makeBooleanValue() {
        return this;
    }

    public CharValue makeCharValue() {
        return new CharValue(value ? (char)1 : (char)0);
    }

    public BooleanValue getNegated() {
        return new BooleanValue(value ? false : true);
    }

    public String getAssembly() {
        if (value) 
            return "1";
        else 
            return "0";
    }

}

