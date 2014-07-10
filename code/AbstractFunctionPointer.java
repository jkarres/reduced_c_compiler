import java.util.Vector;

abstract class AbstractFunctionPointer implements Callable, MLV {

    protected FunctionType type;

    public AbstractFunctionPointer(FunctionType type) {
        this.type = type;
    }

    public FunctionType getType() {
        return type;
    }

    public Type getEquivType() {
        return type.untypedef();
    }
    
    abstract protected void putAddrInto(Register r, AssemblyWriter aw, SymbolTable symtab);

    public Expr call(Vector<Expr> args, AssemblyWriter aw, SymbolTable symtab) {
        
        aw.write("\t!\t" + "Entering AbstractFunctionPointer.call");

        //
        // first check whether this is a null pointer
        //
        
        aw.write("\t!\t" + "doing null pointer check");

        String noProblemLabel = LabelMaker.getLabel();

        putAddrInto(Register.L0, aw, symtab);
        aw.write("\t" + "ld\t" + "[" + Register.L0 + "], " + Register.L0);

        aw.write("\t" + "cmp\t" + Register.L0 + ", " + Register.G0);
        aw.write("\t" + "bne\t" + noProblemLabel);
        aw.write("\t" + "nop");

        aw.write("\t" + "set\t" + "__null_pointer_dereference, " + Register.O0);
        aw.write("\t" + "call\t" + "printf");
        aw.write("\t" + "nop");
        aw.write("\t" + "mov\t" + 1 + ", " + Register.O0);
        aw.write("\t" + "call\t" + "exit");
        aw.write("\t" + "nop");

        aw.write(noProblemLabel + ":");

        aw.write("\t!\t" + "finished with null pointer check");

        String implicitArgumentCallLabel = LabelMaker.getLabel();
        String doneLabel = LabelMaker.getLabel();
        Expr retval;

        //
        // copy arguments into temporaries
        //

        Vector<ParamDecl> params = type.getParameters();

        int[] temps = new int[args.size()];
        for (int i = 0; i < args.size(); ++i) {
            temps[i] = symtab.getTemp(4);
            if (params.get(i).getIsRef())
                args.get(i).putAddrInto(temps[i], aw, symtab);
            else
                args.get(i).putValueInto(temps[i], aw, symtab);
        }

        //
        // figure out how to do the actual function call
        //

        putAddrInto(Register.L0, aw, symtab);

        // %l0 - this address
        // %l1 - [%l0 + 4] -- whether there is an implicit argument
        // %l2 - [%l0] - the address of the function

        aw.write("\t" + "ld\t" + "[" + Register.L0 + " + " + 4 + "], " + Register.L1);
        aw.write("\t" + "cmp\t" + Register.L1 + ", " + Register.G0);
        aw.write("\t" + "bne\t" + implicitArgumentCallLabel + "\t! implicitArgumentCallLabel");
        aw.write("\t" + "nop");

        //
        // no implicit argument
        //

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


        // make the function call
        aw.write("\t" + "ld\t" + "[" + Register.L0 + "], " + Register.L2);
        aw.write("\t" + "jmpl\t" + Register.L2 + ", %o7");
        aw.write("\t" + "nop");

        if (extraArgCount > 0) {
            aw.write("\t" + "sub\t" + "%sp, -(" + extraArgCount + "*4) & -8, %sp");
        }

        aw.write("\t" + "ba\t" + doneLabel + "\t! doneLabel");
        aw.write("\t" + "nop");

        //
        // yes implicit argument
        //
        aw.write(implicitArgumentCallLabel + ":\t! implicitArgumentCallLabel");

        // copy the implicit argument in
        aw.write("\t" + "ld\t" + "[" + Register.L0 + " + " + 8 + "], " + Register.O0);

        // copy the values into the appropriate o registers
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

        extraArgCount = args.size() - 5;

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


        // make the function call
        aw.write("\t" + "ld\t" + "[" + Register.L0 + "], " + Register.L2);
        aw.write("\t" + "jmpl\t" + Register.L2 + ", %o7");
        aw.write("\t" + "nop");
        
        if (extraArgCount > 0) {
            aw.write("\t" + "sub\t" + "%sp, -(" + extraArgCount + "*4) & -8, %sp");
        }

        // and now we're done with the actual call
        aw.write(doneLabel + ":\t! doneLabel");
        aw.write("");

        int retval_temp = symtab.getTemp(4);
        aw.storeLocal(Register.O0, retval_temp);

        Type returnType = type.getReturnType();

        if (returnType instanceof VoidType) {
            retval = new VoidExpr();
        } else {
            NCRV theRetval = new NCRV(returnType);
            retval = theRetval;
            theRetval.setLocation(retval_temp);
        }

        aw.write("");
        aw.write("\t!\t" + "Leaving AbstractFunctionPointer.call");
        aw.write("");

        return retval;

    }

    public void assignInto(AbstractFunctionPointer lhs, AssemblyWriter aw,
                           SymbolTable symtab) {

        aw.write("");
        aw.write("\t!\t" + "FunctionPointer.assignInto");

        int lhsAddr_temp = symtab.getTemp(4);
        lhs.putAddrInto(lhsAddr_temp, aw, symtab);

        // put this address into L1
        putAddrInto(Register.L1, aw, symtab);

        // put lhs address into L0
        aw.loadLocal(lhsAddr_temp, Register.L0);
        
        for (int i = 0; i < 3; ++i) {
            aw.write("\t" + "ld\t" + "[" + Register.L1 + " + " + i*4 + "], " + Register.L2);
            aw.write("\t" + "st\t" + Register.L2 + ", [" + Register.L0 + " + " + i*4 + "]");
        }

    }



}
