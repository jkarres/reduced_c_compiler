import java.util.Vector;

// you can't put these in the symbol table -- they are the result of
// taking something out of the symbol table and giving it its implicit
// argument.

class BoundFunction implements Callable {

    // we need this to know how to interpret the arguments
    private FunctionType type;

    // the name that will actually be handed to the assembler
    private String fullName;

    private Expr implicitArgument;

    public BoundFunction(String fullName, FunctionType type, 
                         Expr implicitArgument)
    {
        //System.out.println("In BoundFunction constructor, fullname: "  + fullName);


        this.fullName = fullName;
        this.type = type;
        this.implicitArgument = implicitArgument;
    }

    public Expr call(Vector<Expr> args, AssemblyWriter aw, SymbolTable symtab) {

        //System.out.println("Entering BoundFunction.call");

        // get addr of implicit arg
        int implicitArg_temp = symtab.getTemp(4);
        implicitArgument.putAddrInto(implicitArg_temp, aw, symtab);

        // get the value/addr of each of the explicit args
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
        aw.loadLocal(implicitArg_temp, Register.O0);

        for (int i = 0; i < args.size() && i < 5; ++i) {

            if (params.get(i).getType().untypedef() instanceof FloatType &&
                args.get(i).getType().untypedef() instanceof IntType)
            {
                aw.loadLocal(temps[i], Register.F0);
                aw.write("\t" + "fitos\t" + Register.F0 + ", " + Register.F0);
                aw.storeLocal(Register.F0, temps[i]);
            }

            aw.loadLocal(temps[i], Register.O[i+1]);
        }

        int extraArgCount = args.size() - 5;

        if (extraArgCount > 0) {
            aw.write("\t" + "add\t" + "%sp, -(" + extraArgCount + 
                     "*4) & -8, %sp");

            for (int i = 5; i < args.size(); ++i) {
                if (params.get(i).getType().untypedef() instanceof FloatType &&
                    args.get(i).getType().untypedef() instanceof IntType)
                {
                    aw.loadLocal(temps[i], Register.F0);
                    aw.write("\t" + "fitos\t" + Register.F0 + ", " + Register.F0);
                    aw.storeLocal(Register.F0, temps[i]);
                }
                
                aw.loadLocal(temps[i], Register.G1);
                aw.write("\t" + "st\t" + Register.G1 + ", [%sp + " + (92 + (i-5)*4) + "]");
            }
            
        }

        aw.write("\t" + "call" + "\t" + fullName);
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

    public void putAddrInto(int offset, AssemblyWriter aw, SymbolTable st) {
        assert false;
    }

    public void putValueInto(int offset, AssemblyWriter aw, SymbolTable st) {
        assert false;
    }

    public FunctionType getType() { 
        return type;
    }

    public Type getEquivType() {
        return type.untypedef();
    }


    public void assignInto(AbstractFunctionPointer lhs, AssemblyWriter aw,
                           SymbolTable symtab)
    {
        aw.write("");
        aw.write("\t!\t" + "BoundFunction.assignInto");


        int lhsAddr_temp = symtab.getTemp(4);
        lhs.putAddrInto(lhsAddr_temp, aw, symtab);

        int arg_temp = symtab.getTemp(4);
        implicitArgument.putAddrInto(arg_temp, aw, symtab);

        // put lhs address into L0
        aw.loadLocal(lhsAddr_temp, Register.L0);
        
        // write function address into [L0]
        aw.write("\t" + "set\t" + fullName + ", " +  Register.L1);
        aw.write("\t" + "st\t" + Register.L1 + ", " + "[" + Register.L0 + "]");

        // write 1 into [L0 + 4]
        aw.write("\t" + "set\t" + 1 + ", " + Register.L2);
        aw.write("\t" + "st\t" + Register.L2 + ", " + "[" + Register.L0 + " + " + 4 + "]");

        // write addr of implicit argument into [L0 + 8]
        aw.loadLocal(arg_temp, Register.L3);
        aw.write("\t" + "st\t" + Register.L3 + ", " + "[" + Register.L0 + " + " + 8 + "]");

    }


}
