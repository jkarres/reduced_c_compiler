class ArgumentMemoryLocation extends MemoryLocation {
    
    /// positive, relative to %fp
    int offset;

    /// o is relative to 68
    ArgumentMemoryLocation(int o) {
        offset = 68 + o;
    }

    void load(Register r, AssemblyWriter aw) {
        load(r, 0, aw);
    }

    void load(Register r, int o, AssemblyWriter aw) {
        aw.loadLocal(-(offset + o), r);
    }

    void putAddress(Register r, AssemblyWriter aw) {
        putAddress(r, 0, aw);
    }

    void putAddress(Register r, int o, AssemblyWriter aw) {
        aw.write("\t" + "add" + "\t" + "%fp, " + (offset + o) + ", " + r);
    }

    void store(Register r, AssemblyWriter aw) {
        store(r, 0, aw);
    }

    void store(Register r, int o, AssemblyWriter aw) {
        aw.storeLocal(r, -(offset + o));
    }

}
