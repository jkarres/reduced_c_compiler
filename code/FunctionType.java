import java.util.Vector;

public class FunctionType extends Type {
    private Type returnType;
    private Vector<ParamDecl> parameters;

    public FunctionType(Type returnType, Vector<ParamDecl> parameters) {
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public FunctionType(FunctionType ft) {
        this.returnType = ft.returnType;
        this.parameters = ft.parameters;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Vector<ParamDecl> getParameters() {
        return parameters;
    }

    public String getName() {
        return "funcptr : " + 
            returnType.getName() + " (" + stringify(parameters) + ")";
    }

    public boolean isExactlyEquivalent(Type t) {
        if (t instanceof FunctionType) {
            FunctionType ft = (FunctionType)t;
            if (((returnType instanceof VoidType && ft.returnType instanceof VoidType) ||
                 (returnType.isExactlyEquivalent(ft.returnType))) &&
                parameters.size() == ft.parameters.size())
            {
                for (int i = 0; i < parameters.size(); ++i) {
                    if (!parameters.get(i).isExactlyEquivalent(ft.parameters.get(i)))
                        return false;
                }
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    public Type untypedef() {
        return new FunctionType(returnType.untypedef(), untypedefAll(parameters));
    }

    public String toString() {
        String retval =
            "<FunctionType>" + 
            "<returnType>" + returnType.toString() + "</returnType>" +
            "<parameters>";
        for (ParamDecl param : parameters)
            retval += param.toString();
        retval += "</parameters></FunctionType>";
        return retval;
    }

    private static String stringify(Vector<ParamDecl> params) {
        if (params.size() == 0) {
            return "";
        } else if (params.size() == 1) {
            return params.get(0).getDescription();
        } else {
            String retval = params.get(0).getDescription();
            for (int i = 1; i < params.size(); ++i) {
                retval += ", " + params.get(i).getDescription();
            }
            return retval;
        }
    }

    private static Vector<ParamDecl> untypedefAll(Vector<ParamDecl> parameters) {
        Vector<ParamDecl> retval = new Vector<ParamDecl>();
        for (ParamDecl p : parameters) {
            retval.add(p.untypedef());
        }
        return retval;
    }

    public ObjectValue makeValueFrom(ObjectValue v) { 
        assert false;
        return null;
    }

    public int size() { return 12; }

    public boolean isAssignableFrom(Type t) {
        if (super.isAssignableFrom(t))
            return true;

        if (t instanceof NullType)
            return true;

        return false;
    }

}
