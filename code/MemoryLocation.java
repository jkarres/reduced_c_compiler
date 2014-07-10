abstract class MemoryLocation {

    /// return assembly that loads this contents of (this location +
    /// offset) into register r
    abstract void load(Register r, AssemblyWriter aw);
    abstract void load(Register r, int offset, AssemblyWriter aw);

    /// return assembly that loads (this address + offset, AssemblyWriter aw) into
    /// register r
    abstract void putAddress(Register r, AssemblyWriter aw);
    abstract void putAddress(Register r, int offset, AssemblyWriter aw);

    /// return assembly that stores the contents of register r int
    /// (this location + offset, AssemblyWriter aw)
    abstract void store(Register r, AssemblyWriter aw);
    abstract void store(Register r, int offset, AssemblyWriter aw);
    
}
