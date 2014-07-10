public class CharValue extends ObjectValue {
    private final char value;

    public CharValue(char value) {
        this.value = value;
    }

    public char getValue() {
        return value;
    }

    public Type getType() {
        return new CharType();
    }
    public IntValue makeIntValue() {
        return new IntValue((int)value);
    }

    public FloatValue makeFloatValue() {
        return new FloatValue((float)value);
    }

    public BooleanValue makeBooleanValue() {
        return new BooleanValue(value == 0);
    }

    public CharValue makeCharValue() {
        return this;
    }

    public String getAssembly() {
        return "'" + value + "'";
    }

}
