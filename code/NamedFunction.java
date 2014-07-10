import java.util.Vector;

// this is something you can pull straight out of the symbol table

class NamedFunction implements Expr, FunctionSTO, Callable {
    private String name;
    private FunctionType type;

    NamedFunction(String name, FunctionType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return name;
    }

    public FunctionType getType() { 
        return type;
    }

    public Type getEquivType() {
        return type.untypedef();
    }

    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) {
        assert false;
    }

    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {
        assert false;
    }

    public Expr call(Vector<Expr> args, AssemblyWriter aw, SymbolTable symtab) {
        
        Vector<ParamDecl> params = type.getParameters();

        int[] temps = new int[args.size()];
        for (int i = 0; i < args.size(); ++i) {
            temps[i] = symtab.getTemp(4);
            if (params.get(i).getIsRef())
                args.get(i).putAddrInto(temps[i], aw, symtab);
            else
                args.get(i).putValueInto(temps[i], aw, symtab);
        }
        
        // copy those values into the appropriate o registers
        for (int i = 0; i < args.size() && i < 6; ++i) {

            if (params.get(i).getType().untypedef() instanceof FloatType &&
                args.get(i).getType().untypedef() instanceof IntType)
            {
                aw.loadLocal(temps[i], Register.F0);
                aw.write("\t" + "fitos\t" + Register.F0 + ", " + Register.F0);
                aw.storeLocal(Register.F0, temps[i]);
            }

            aw.loadLocal(temps[i], Register.O[i]);
        }

        int extraArgCount = args.size() - 6;

        if (extraArgCount > 0) {

            aw.write("\t" + "add\t" + "%sp, -(" + extraArgCount + 
                     "*4) & -8, %sp");

            for (int i = 6; i < args.size(); ++i) {
                if (params.get(i).getType().untypedef() instanceof FloatType &&
                    args.get(i).getType().untypedef() instanceof IntType)
                {
                    aw.loadLocal(temps[i], Register.F0);
                    aw.write("\t" + "fitos\t" + Register.F0 + ", " + Register.F0);
                    aw.storeLocal(Register.F0, temps[i]);
                }
                
                aw.loadLocal(temps[i], Register.G1);
                aw.write("\t" + "st\t" + Register.G1 + ", [%sp + " + (92 + (i-6)*4) + "]");
            }
        }

        aw.write("\t" + "call" + "\t" + name);
        aw.write("\t" + "nop");

        if (extraArgCount > 0) {
            aw.write("\t" + "sub\t" + "%sp, -(" + extraArgCount + "*4) & -8, %sp");
        }

        int retval_temp = symtab.getTemp(4);
        aw.storeLocal(Register.O0, retval_temp);

        Expr retval;
        Type returnType = type.getReturnType();

        if (returnType instanceof VoidType) {
            retval = new VoidExpr();
        } else {
            NCRV theRetval = new NCRV(returnType);
            retval = theRetval;
            theRetval.setLocation(retval_temp);
        }

        return retval;

    }

    public void assignInto(AbstractFunctionPointer lhs, AssemblyWriter aw,
                           SymbolTable symtab) {

        aw.write("");
        aw.write("\t!\t" + "NamedFunction.assignInto");

        int lhsAddr_temp = symtab.getTemp(4);
        lhs.putAddrInto(lhsAddr_temp, aw, symtab);

        // put lhs address into L0
        aw.loadLocal(lhsAddr_temp, Register.L0);

        // write this address into [L0]
        aw.write("\t" + "set\t" + name + ", " +  Register.L1);
        aw.write("\t" + "st\t" + Register.L1 + ", " + "[" + Register.L0 + "]");

        // write 0 into [L0+4] and [L0+8]
        aw.write("\t" + "st\t" + Register.G0 + ", " + "[" + Register.L0 + " + " + 4 + "]");
        aw.write("\t" + "st\t" + Register.G0 + ", " + "[" + Register.L0 + " + " + 8 + "]");
        
    }

    

}
