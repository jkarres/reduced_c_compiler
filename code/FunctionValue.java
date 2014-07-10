public class FunctionValue extends Value {

    public final FunctionType type;
    public final String name;
    
    public FunctionValue(FunctionType type, String name) {
        this.type = type;
        this.name = name;
    }

    public FunctionType getType() {
        return type;
    }

    public String toString() {
        return "<FunctionValue><type>" + 
            type.toString() + "</type></FunctionValue>";
    }

    public String getAssembly() {
        return name;
    }

}