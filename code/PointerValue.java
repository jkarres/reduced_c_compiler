class PointerValue extends ObjectValue {
    private int value;
    private PointerType type;

    public PointerValue(PointerType type, int value) {
        this.type = type;
        this.value = value;
    }

    public PointerType getType() {
        return type;
    }

    public IntValue makeIntValue() {
        return new IntValue(value);
    }

    public FloatValue makeFloatValue() {
        return new FloatValue((float) value);
    }

    public BooleanValue makeBooleanValue() {
        return new BooleanValue(value == 0 ? false : true);
    }

    public CharValue makeCharValue() {
        return new CharValue((char) value);
    }

    public int getValue() { return value; }

    public String getAssembly() {
        return "" + value;
    }

}
