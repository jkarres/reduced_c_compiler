public class IntValue extends NumericValue implements IntValueOrNothingOrReportedErr {
    private final int value;

    public IntValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public Type getType() {
        return new IntType();
    }

    public NumericValue addMul(Operator op, NumericValue rhs) {
        assert op == Operator.PLUS || op == Operator.MINUS || op == Operator.STAR || op == Operator.SLASH;
        return rhs.addMulInt(op, this);
    }

    public NumericValue addMulInt(Operator op, IntValue lhs) {
        if (op == Operator.PLUS)
            return new IntValue(lhs.value + this.value);
        if (op == Operator.MINUS)
            return new IntValue(lhs.value - this.value);
        if (op == Operator.STAR)
            return new IntValue(lhs.value * this.value);
        if (op == Operator.SLASH)
            return new IntValue(lhs.value / this.value);
        assert false;
        return null;
    }

    public NumericValue addMulFloat(Operator op, FloatValue lhs) {
        if (op == Operator.PLUS)
            return new FloatValue(lhs.getValue() + this.value);
        if (op == Operator.MINUS)
            return new FloatValue(lhs.getValue() - this.value);
        if (op == Operator.STAR)
            return new FloatValue(lhs.getValue() * this.value);
        if (op == Operator.SLASH)
            return new FloatValue(lhs.getValue() / this.value);
        assert false;
        return null;
    }

    public BooleanValue relation(Operator op, NumericValue rhs) {
        assert op == Operator.LT || op == Operator.LTE || op == Operator.GT || op == Operator.GTE;
        return rhs.relationInt(op, this);
    }
    
    public BooleanValue relationFloat(Operator op, FloatValue lhs) {
        if (op == Operator.LT)
            return new BooleanValue(lhs.getValue() < this.getValue());
        if (op == Operator.LTE)
            return new BooleanValue(lhs.getValue() <= this.getValue());
        if (op == Operator.GT)
            return new BooleanValue(lhs.getValue() > this.getValue());
        if (op == Operator.GTE)
            return new BooleanValue(lhs.getValue() >= this.getValue());
        assert false;
        return null;
    }

    public BooleanValue relationInt(Operator op, IntValue lhs) {
        if (op == Operator.LT)
            return new BooleanValue(lhs.getValue() < this.getValue());
        if (op == Operator.LTE)
            return new BooleanValue(lhs.getValue() <= this.getValue());
        if (op == Operator.GT)
            return new BooleanValue(lhs.getValue() > this.getValue());
        if (op == Operator.GTE)
            return new BooleanValue(lhs.getValue() >= this.getValue());
        assert false;
        return null;
    }


    public BooleanValue equality(Operator op, NumericValue rhs) {
        assert op == Operator.EQU || op == Operator.NEQ;
        return rhs.equalityInt(op, this);
    }
    
    public BooleanValue equalityFloat(Operator op, FloatValue lhs) {
        if (op == Operator.EQU)
            return new BooleanValue(lhs.getValue() == this.getValue());
        if (op == Operator.NEQ)
            return new BooleanValue(lhs.getValue() != this.getValue());
        assert false;
        return null;
    }

    public BooleanValue equalityInt(Operator op, IntValue lhs) {
        if (op == Operator.EQU)
            return new BooleanValue(lhs.getValue() == this.getValue());
        if (op == Operator.NEQ)
            return new BooleanValue(lhs.getValue() != this.getValue());
        assert false;
        return null;
    }


    public String toString() {
        return "<IntValue>" + value + "</IntValue>";
    }

    public IntValue makeIntValue() {
        return this;
    }

    public FloatValue makeFloatValue() {
        return new FloatValue((float)value);
    }

    public BooleanValue makeBooleanValue() {
        return new BooleanValue(value != 0);
    }

    public CharValue makeCharValue() {
        return new CharValue((char)value);
    }

    public IntValue getNegated() {
        return new IntValue(-value);
    }

    public String getAssembly() {
        return "" + value;
    }

}
