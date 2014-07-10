public class NameAndType {
    public String name;
    public Type type;
    NameAndType(String name, Type type) {
        this.name = name;
        this.type = type;
    }
    public String toString() {
        return "<NameAndType>" +
            "<name>" + name + "</name>" +
            "<type>" + type.toString() + "</type>" +
            "</NameAndType>";
    }
}

