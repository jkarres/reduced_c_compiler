import java.util.*;
import java.text.*;

public class FloatValue extends NumericValue {
    private final float value;

    public FloatValue(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public Type getType() {
        return new FloatType();
    }

    public NumericValue addMul(Operator op, NumericValue rhs) {
        assert op == Operator.PLUS || op == Operator.MINUS || op == Operator.STAR || op == Operator.SLASH;
        return rhs.addMulFloat(op, this);
    }

    public NumericValue addMulInt(Operator op, IntValue lhs) {
        if (op == Operator.PLUS)
            return new FloatValue(lhs.getValue() + this.value);
        if (op == Operator.MINUS)
            return new FloatValue(lhs.getValue() - this.value);
        if (op == Operator.STAR)
            return new FloatValue(lhs.getValue() * this.value);
        if (op == Operator.SLASH) {
            if (this.value == 0.0) throw new ArithmeticException();
            return new FloatValue(lhs.getValue() / this.value);
        }
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
        if (op == Operator.SLASH) {
            if (this.value == 0.0) throw new ArithmeticException();
            return new FloatValue(lhs.getValue() / this.value);
        }
        assert false;
        return null;
    }

    public BooleanValue relation(Operator op, NumericValue rhs) {
        assert op == Operator.LT || op == Operator.LTE || op == Operator.GT || op == Operator.GTE;
        return rhs.relationFloat(op, this);
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
        return rhs.equalityFloat(op, this);
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
        return "<FloatValue>" + value + "</FloatValue>";
    }

    public IntValue makeIntValue() {
        return new IntValue((int)value);
    }

    public FloatValue makeFloatValue() {
        return this;
    }

    public BooleanValue makeBooleanValue() {
        return new BooleanValue(value != 0.0);
    }

    public CharValue makeCharValue() {
        return new CharValue((char)value);
    }

    public FloatValue getNegated() {
        return new FloatValue(-value);
    }

    static Vector<NameLiteralPair> nlps = new Vector<NameLiteralPair>();

    static String getFloatLiteralAssembly() {

        String retval = new String();
        retval += "\t.section\t\".rodata\"\n";
        retval += "\t.align\t4\n";
        for (NameLiteralPair nlp : nlps) {

            retval += nlp.name + ":\n";
            retval += "\t" + nlp.literal + "\n";
        }

        return retval;

    }

    public String getAssembly() {
        NameLiteralPair nlp = NameLiteralPair.make(value);
        nlps.add(nlp);

        String retval = new String();

        retval += nlp.name + ", " + Register.G2 + "\n";
        retval += "\t" + "ld" + "\t" + "[" + Register.G2 + "]";

        return retval;
    }

}

class NameLiteralPair {
    public String name;
    public String literal;

    static int next_name = 0;

    static NameLiteralPair make(float value) {

        NameLiteralPair retval = new NameLiteralPair();
        retval.name = "__float_literal_" + ++next_name;
        retval.literal = ".single\t0r" + value;

        return retval;
    }
        

}