public abstract class NumericValue extends ObjectValue {
    abstract NumericValue addMul(Operator op, NumericValue rhs);
    abstract NumericValue addMulInt(Operator op, IntValue lhs);
    abstract NumericValue addMulFloat(Operator op, FloatValue lhs);

    abstract BooleanValue relation(Operator op, NumericValue rhs);
    abstract BooleanValue relationFloat(Operator op, FloatValue lhs);
    abstract BooleanValue relationInt(Operator op, IntValue lhs);

    abstract BooleanValue equality(Operator op, NumericValue rhs);
    abstract BooleanValue equalityFloat(Operator op, FloatValue lhs);
    abstract BooleanValue equalityInt(Operator op, IntValue lhs);
}
