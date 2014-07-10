public class ErrorSTO implements STO {
    private String name;

    public ErrorSTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
