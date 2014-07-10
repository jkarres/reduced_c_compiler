
public class FunctionScope extends Scope {
    private FunctionSTO func;
    private boolean seenReturn = false;
    private int stackSpace = 0;
    private String name;

    public FunctionScope(FunctionSTO func, boolean inStruct) {
        name = func.getFullName();

        Type t = (FunctionType)((Expr)func).getType();
        assert t instanceof FunctionType;
        this.func = func;

        FunctionType ft = (FunctionType)t;
        int arg_num = 0;

        if (inStruct) {
            arg_num++;
        }

        for (ParamDecl pd : ft.getParameters()) {

            NCVar var = new NCVar(pd.getType(), pd.getName());

            var.setLocation(new ArgumentMemoryLocation(4 * arg_num++));

            if (pd.getIsRef()) {

                if (pd.getType().untypedef() instanceof ArrayType) {
                    ArrayReference ar = 
                        new ArrayReference(new ArgumentMemoryLocation(4 * (arg_num-1)),
                                           pd.getName(), pd.getType());
                    InsertLocal(ar);

                } else {
                    NCDerefRes ref = new NCDerefRes(var);
                    ref.setOperand(var);
                    
                    InsertLocal(ref);
                }

            } else {

                InsertLocal(var);
            }

        }


    }

    public void setSeenReturn() {
        seenReturn = true;
    }

    public boolean getSeenReturn() {
        return seenReturn;
    }

    public void allocate(HasLocation o, AssemblyWriter aw, boolean isStatic) {

        if (isStatic) {
            String mangledName = "__"+name+"_"+o.getName();
            o.setLocation(new NonlocalMemoryLocation(mangledName));

            aw.write("\t" + ".section\t" + "\".bss\"");
            aw.write("\t" + ".align\t" + 4);
            aw.write(mangledName + ":");
            aw.write("\t" + ".skip\t" + o.getSize());
            aw.write("\t" + ".section\t" + "\".text\"");

        } else {
            stackSpace += o.getSize();
            aw.write("\t!\t" + o.getName() + " at %fp - " + stackSpace);
            o.setLocation(new LocalMemoryLocation(stackSpace));
        }
    }

    public int allocate(int spaceNeeded, AssemblyWriter aw) {
        stackSpace += spaceNeeded;
        return stackSpace;
    }


    public int getStackSpace() {
        return stackSpace;
    }

    public String getName() {
        return name;
    }

}
