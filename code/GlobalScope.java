
public class GlobalScope extends Scope {

    public void allocate(HasLocation o, AssemblyWriter aw, boolean isStatic) {

        if (o instanceof CVar && ((CVar)o).getType() instanceof FunctionType)
            return;
        // emit assembly for this
        /// todo: get the size and alignment right
        aw.writeGlobal(o.getName(), o.getSize(), 4, isStatic);

        // set the thing in o
        o.setLocation(new NonlocalMemoryLocation(o.getName()));
    }

}
