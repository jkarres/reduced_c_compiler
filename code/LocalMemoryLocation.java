class LocalMemoryLocation extends MemoryLocation {

    /// *positive*
    int offset;

    LocalMemoryLocation(int o) {
        offset = o;
    }

    void load(Register r, AssemblyWriter aw) {
        load(r, 0, aw);
    }

    void load(Register r, int o, AssemblyWriter aw) {
        aw.loadLocal(offset - o, r);
    }

    void putAddress(Register r, AssemblyWriter aw) {
        putAddress(r, 0, aw);
    }

    void putAddress(Register r, int o, AssemblyWriter aw) {
        aw.subFromFramePointer(offset - o, r);
    }

    void store(Register r, AssemblyWriter aw) {
        store(r, 0, aw);
    }

    void store(Register r, int o, AssemblyWriter aw) {
        aw.storeLocal(r, offset - o);
    }

}
