public class ParamDecl implements ParamDeclOrErr, ParamDeclOrReportedErr {
    private Type type;
    private String id;
    private boolean isRef;

    public ParamDecl(Type type, String id, boolean isRef) {
        this.type = type;
        this.id = id;
        this.isRef = isRef;
    }

    public Type getType() {
        return type;
    }

    public boolean getIsRef() {
        return isRef;
    }

    public String toString() {
        return "<ParamDecl>" +
            "<type>" + type.toString() + "</type>" +
            "<id>" + id + "</id>" +
            "<isRef>" + isRef + "</isRef>" +
            "</ParamDecl>";
    }

    // returns whether other has the same type
    public boolean isExactlyEquivalent(ParamDecl other) {
        return type.isExactlyEquivalent(other.type) && 
            (isRef == other.isRef);
    }

    public String getDescription() {
        return type.getName() + " " + (isRef ? "&" : "") + id;
    }

    public String getName() {
        return id;
    }

    public ParamDecl untypedef() {
        return new ParamDecl(type.untypedef(), id, isRef);
    }

}
