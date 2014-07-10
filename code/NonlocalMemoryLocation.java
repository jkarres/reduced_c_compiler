class NonlocalMemoryLocation extends MemoryLocation {
    
    String label;

    NonlocalMemoryLocation(String s) {
        label = s;
    }

    void load(Register r, AssemblyWriter aw) {
        load(r, 0, aw);
    }

    void load(Register r, int o, AssemblyWriter aw) {
        String source;
        if (o == 0) source = Register.G1.toString();
        else source = Register.G1.toString() + " + " + o;

        aw.write("\t" + "set" + "\t" + label + ", " + Register.G1);
        aw.write("\t" + "ld" + "\t" + "[" + source + "], " + r);


    }

    void putAddress(Register r, int o, AssemblyWriter aw) {

        aw.write("\t" + "set" + "\t" + label + ", " + r);
        aw.write("\t" + "add" + "\t" + r + ", " + o + ", " + r);
    }

    void putAddress(Register r, AssemblyWriter aw) {
        aw.write("\t" + "set" + "\t" + label + ", " + r);
    }

    void store(Register r, AssemblyWriter aw) {
        store(r, 0, aw);
    }

    void store(Register r, int o, AssemblyWriter aw) {
        String destination;
        if (o == 0) destination = Register.G1.toString();
        else destination = Register.G1.toString() + " + " + o;

        aw.write("\t" + "set" + "\t" + label + ", " + Register.G1);
        aw.write("\t" + "st" + "\t" + r + ", " + "[" + destination + "]");
    }

}
