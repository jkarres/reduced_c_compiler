import java.util.*;

public class StringValue extends ObjectValue {
    private final String value;

    public StringValue(String s) {
        this.value = s;
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return new PointerType(new CharType());
    }
    public IntValue makeIntValue() {
        assert false;
        return null;
    }

    public FloatValue makeFloatValue() {
        assert false; 
        return null;
    }

    public BooleanValue makeBooleanValue() {
        assert false;
        return null;
    }

    public CharValue makeCharValue() {
        assert false;
        return null;
    }

    static Vector<NameStringLiteralPair> nlps = 
        new Vector<NameStringLiteralPair>();

    static String getStringLiteralAssembly() {
        String retval = new String();
        retval += "\t.section\t\".rodata\"\n";
        for (NameStringLiteralPair nlp : nlps) {

            retval += nlp.name + ":\n";
            retval += "\t" + nlp.literal + "\n";
        }

        return retval;
    }

    public String getAssembly() {
        NameStringLiteralPair nlp = NameStringLiteralPair.make(value);
        nlps.add(nlp);
        return nlp.name;
    }
        
}


class NameStringLiteralPair {
    public String name;
    public String literal;

    static int next_name = 0;

    static NameStringLiteralPair make(String value) {

        NameStringLiteralPair retval = new NameStringLiteralPair();
        retval.name = "__string_literal_" + ++next_name;

        retval.literal = ".asciz" + "\t" + "\"" + value + "\"";

        return retval;
    }
        

}