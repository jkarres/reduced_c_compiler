class LabelMaker {

    private static int nextLabelNumber = 0;

    public static String getLabel() {
        return ".L" + ++nextLabelNumber;
    }
}
