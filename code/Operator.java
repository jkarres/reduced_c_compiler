import java.util.*;

public enum Operator {

    PLUS("+"), MINUS("-"), STAR("*"), SLASH("/"), MOD("%"), LT("<"), 
        LTE("<="), GT(">"), GTE(">="), EQU("=="), NEQ("!="), 
        OR("||"), AND("&&"), NOT("!"), AMPERSAND("&"), CARET("^"), 
        BAR("|"), PLUSPLUS("++"), MINUSMINUS("--")
                ;

    private static Map<String, Operator> map;

    static {
        map = new TreeMap<String, Operator>();
        map.put("+", PLUS);
        map.put("-",MINUS);
        map.put("*",STAR);
        map.put("/",SLASH);
        map.put("%",MOD);
        map.put("<",LT);
        map.put("<=",LTE);
        map.put(">",GT);
        map.put(">=",GTE);
        map.put("==",EQU);
        map.put("!=",NEQ);
        map.put("||",OR);
        map.put("&&",AND);
        map.put("!",NOT);
        map.put("&",AMPERSAND);
        map.put("^",CARET);
        map.put("|",BAR);
    }

    static public Operator get(String s) {
        return map.get(s);
    }

    private String symbol;

    public String toString() {
        return symbol;
    }

    Operator(String s) {
        symbol = s;
    }
       
}
        