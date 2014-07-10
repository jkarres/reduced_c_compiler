//---------------------------------------------------------------------
//
//---------------------------------------------------------------------

import java_cup.runtime.*;
import java.util.Stack;
import java.util.Vector;


class MyParser extends parser
{
    //----------------------------------------------------------------
    //	Instance variables
    //----------------------------------------------------------------
	private Lexer m_lexer;
	private ErrorPrinter m_errors;
	private int	m_nNumErrors;
	private String m_strLastLexeme;
	private boolean m_bSyntaxError = true;
	private int m_nSavedLineNum;

	private SymbolTable m_symtab;

    public AssemblyWriter aw;
    public AssemblyWriter directWriter;
    public AssemblyWriter initWriter = new AssemblyWriter(true);
    
    private Stack<String> andLabels = new Stack<String>();
    private Stack<String> orLabels = new Stack<String>();
    private Stack<String> elseLabels = new Stack<String>();
    private Stack<String> afterElseLabels = new Stack<String>();
    private Stack<String> forLabelBases = new Stack<String>();

    private boolean isStatic = false;
    private String endLocalStaticInitLabel = null;


    public static String sizeName(String structName) {
        return "SIZEOF_" + structName;
    }

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public MyParser (Lexer lexer, ErrorPrinter errors, AssemblyWriter aw)
	{
		m_lexer = lexer;
		m_symtab = new SymbolTable(this);

		m_errors = errors;
		m_nNumErrors = 0;
        this.directWriter = aw;

        this.aw = initWriter;

        ArrayDeref.setSymbolTable(m_symtab);
	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean Ok ()
	{
		return (m_nNumErrors == 0);
	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public Symbol scan ()
	{
		Token t = m_lexer.GetToken ();

		//	We'll save the last token read for error messages.
		//	Sometimes, the token is lost reading for the next
		//	token which can be null.
		m_strLastLexeme = t.GetLexeme ();

		switch (t.GetCode ())
        {
        case sym.T_ID:
        case sym.T_ID_U:
        case sym.T_STR_LITERAL:
        case sym.T_FLOAT_LITERAL:
        case sym.T_INT_LITERAL:
        case sym.T_CHAR_LITERAL:
            return (new Symbol (t.GetCode (), t.GetLexeme ()));
        default:
            return (new Symbol (t.GetCode ()));
        }
	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void syntax_error (Symbol s)
	{
	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void report_fatal_error (Symbol s)
	{
		m_nNumErrors++;
		if (m_bSyntaxError)
        {
            m_nNumErrors++;

            //	It is possible that the error was detected
            //	at the end of a line - in which case, s will
            //	be null.  Instead, we saved the last token
            //	read in to give a more meaningful error
            //	message.
            m_errors.print (Formatter.toString (ErrorMsg.syntax_error, m_strLastLexeme));
        }
	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void unrecovered_syntax_error (Symbol s)
	{
		report_fatal_error (s);
	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void DisableSyntaxError ()
	{
		m_bSyntaxError = false;
	}

	public void EnableSyntaxError ()
	{
		m_bSyntaxError = true;
	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public String GetFile ()
	{
		return (m_lexer.getEPFilename ());
	}

	public int GetLineNum ()
	{
		return (m_lexer.getLineNumber ());
	}

	public void SaveLineNum ()
	{
		m_nSavedLineNum = m_lexer.getLineNumber ();
	}

	public int GetSavedLineNum ()
	{
		return (m_nSavedLineNum);
	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoProgramStart()
	{
		m_symtab.openGlobalScope();

	}

    


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoProgramEnd()
	{
        aw = directWriter;

        // the init function
        aw.write("");
        aw.write("\t.section\t\".text\"");
        aw.write("__init:");
        aw.write("\tset\tSAVE.__init, " + Register.G1);
        aw.write("\tsave\t" + Register.SP + ", " + Register.G1 + ", " + Register.SP);
        aw.write("");

        // remember that you've already been here
        aw.write("\t" + "set\t" + "__init_done, " + Register.L0);
        aw.write("\t" + "set\t" + 1 + ", " + Register.L1);
        aw.write("\t" + "st\t" + Register.L1 + ", [" + Register.L0 + "]");
        
        initWriter.dump(aw);

        aw.write("\tret");
        aw.write("\trestore");
        aw.write("");
        aw.write("SAVE.__init = -(92 + " + m_symtab.getInitFuncTempSize() + ") & -8");
        aw.write("");

        
        // function in case of bad array access
        // still in .text
        aw.write("__bad_array_access:");
        aw.write("\t" + "set\t" + "__bad_array_access_message, " + Register.O0);
        aw.write("\t" + "call\t" + "printf");
        aw.write("\t" + "nop");
        aw.write("\t" + "mov\t" + 1 + ", " + Register.O0);
        aw.write("\t" + "call\t" + "exit");
        aw.write("\t" + "nop");
        

        // memory for lowest_sp_yet
        aw.write("\t.section\t\".bss\"");
        aw.write("\t.align\t4");
        aw.write(".lowest_sp_yet:");
        aw.write("\t.skip\t4");


        // string formats needed by everybody
        aw.write("\t.section\t\".rodata\"");
        // no alignment necessary
        aw.write("__int_format:");
        aw.write("\t.asciz\t\"%d\"");
        aw.write("__str_format:");
        aw.write("\t.asciz\t\"%s\"");
        aw.write("__true_string:");
        aw.write("\t.asciz\t\"true\"");
        aw.write("__false_string:");
        aw.write("\t.asciz\t\"false\"");
        aw.write("__bad_array_access_message:");
        aw.write("\t.asciz\t\"Index value of %d is outside legal range [0,%d).\\n\"");
        aw.write("__null_pointer_dereference:");
        aw.write("\t.asciz\t\"Attempt to dereference NULL pointer.\\n\"");
        aw.write("__deallocated_stack_pointer_dereference:");
        aw.write("\t.asciz\t\"Attempt to dereference a pointer into deallocated stack space.\\n\"");

        aw.write("");

        aw.write(FloatValue.getFloatLiteralAssembly());
        aw.write(StringValue.getStringLiteralAssembly());

        Scope closedScope = m_symtab.closeScope();
        DebugLogger.log("closing global scope: " + closedScope);
	}


    STO makeNonConstVariable(Type type, String id) {
        if (type.untypedef() instanceof ArrayType) {
            return new ArrayName(type, id);
        } else if (type.untypedef() instanceof FunctionType) {
            return new FunctionPointer((FunctionType)type.untypedef(), id);
        } else {
            return new NCVar(type, id);
        }
    }

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoVarDecl(Vector<NameExprOrNothingOrReportedErrPair> lstIDs, TypeOrReportedErr maybeType)
	{
        if (maybeType instanceof ReportedErr) {
            for (NameExprOrNothingOrReportedErrPair pair : lstIDs) {
                if (m_symtab.accessLocal(pair.name) != null) {
                    m_nNumErrors++;
                    m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, pair.name));
                }
                m_symtab.insert(new ErrorSTO(pair.name));
            }
            return;
        }

        Type type = (Type)maybeType;

		for (NameExprOrNothingOrReportedErrPair pair : lstIDs) {
		
            String id = pair.name;

            if (m_symtab.accessLocal(id) != null) {
                m_nNumErrors++;
                m_errors.print (Formatter.toString(ErrorMsg.redeclared_id, id));
            }

            //
            // reserve memory
            //

            STO sto = makeNonConstVariable(type, id);
            // this emits the assembly that allocates memory (if any)
            m_symtab.insert(sto, isStatic);

            // 
            // do initialization
            //

            ExprOrNothingOrReportedErr possibleExpr = pair.value;
            if (possibleExpr instanceof Expr) {
                Expr e = (Expr) possibleExpr;
                if (!(type.isAssignableFrom(e.getType()))) {
                    m_nNumErrors++;
                    m_errors.print(Formatter.toString(ErrorMsg.error8_Assign,
                                                      e.getType().getName(),
                                                      type.getName()));
                }
                assign((Expr)sto, e);
            }
        }

        if (isStatic && m_symtab.getFunc() != null) {
            aw.write(endLocalStaticInitLabel + ":");
            aw.write("");
        }

        isStatic = false;
	}

    void doFieldDecl(Vector<String> names, TypeOrReportedErr maybeType) {
        // check for a non-type
        if (maybeType instanceof ReportedErr) {
            for (String name : names) {
                if (m_symtab.accessLocal(name) != null) {
                    m_nNumErrors++;
                    m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, name));
                }
                m_symtab.insert(new ErrorSTO(name));
            }
            return;
        }

        Type type = (Type)maybeType;

        // check for a variable of type incompletestructtype
        if (type.hasBareIST()) {
            for (String name : names) {
                m_nNumErrors++;
                m_errors.print(Formatter.toString(ErrorMsg.error13b_Struct, name));
                m_symtab.insert(new ErrorSTO(name));
            }
            return;
        }

        for (String name : names) {
            if (m_symtab.checkStructMember(name)) {
                m_nNumErrors++;
                m_errors.print(Formatter.toString(ErrorMsg.error13a_Struct, name));
            }

            //m_symtab.insert(makeNonConstVariable(type, name));
            /// \todoi think this is actually allowing arrays to be
            /// modified, but that's not being graded for this project

            if (type.untypedef() instanceof FunctionType) {
                m_symtab.insert(new FunctionPointer((FunctionType)type.untypedef(), name));
            } else {
                m_symtab.insert(new NCVar(type, name));
            }

        }

    }



	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoExternDecl(Vector lstIDs, TypeOrReportedErr type)
	{
		for (int i = 0; i < lstIDs.size (); i++)
        {
            String id = (String) lstIDs.elementAt (i);

            if (m_symtab.accessLocal (id) != null)
            {
                m_nNumErrors++;
                m_errors.print (Formatter.toString(ErrorMsg.redeclared_id, id));
            }

            if (type instanceof Type) {
                m_symtab.insertExtern(new NCVar((Type)type, id));
            } else if (type instanceof ReportedErr) {
                m_symtab.insertExtern(new ErrorSTO(id));
            } else {
                assert false;
            }
        }
	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoConstDecl(Vector<NameConstExprOrErrPair> lstIDs, TypeOrReportedErr maybeType)
	{
        if (maybeType instanceof Err) {
            for (NameConstExprOrErrPair pair : lstIDs) {
                if (m_symtab.accessLocal(pair.name) != null) {
                    m_nNumErrors++;
                    m_errors.print (Formatter.toString (ErrorMsg.redeclared_id, pair.name));
                }
                m_symtab.insert(new ErrorSTO(pair.name));
            }
            return;
        }

        Type type = (Type)maybeType;

		for (NameConstExprOrErrPair pair : lstIDs)
        {
            String id = pair.name;

            if (m_symtab.accessLocal(id) != null) {
                m_nNumErrors++;
                m_errors.print (Formatter.toString (ErrorMsg.redeclared_id, id));
            }
		
            if (pair.value instanceof ConstExpr) {
                ConstExpr from = (ConstExpr)pair.value;
                if (type.isAssignableFrom(from.getType())) {
                    Value fromValue = from.getValue();

                    // this is correct since if the fromValue were a
                    // functionvalue, type would not be assignable from it.

                    // this is incorrect since a function pointer can
                    // be assigned from a bare function name without
                    // an ampersand in front; however check 18 b in
                    // the assignment description says that constant
                    // function pointers will not be tested, so I'm
                    // not going to worry about this right now.

                    // then again in the test cases, we do have a
                    // constant function pointer.


                    if (fromValue instanceof FunctionValue) {
                        m_symtab.insert(new CVar(type.makeValueFrom(new IntValue(0)), id, type), isStatic);
                        /// \todo function pointers!
                    } else {
                        assert fromValue instanceof ObjectValue;
                        CVar var = new CVar(type.makeValueFrom((ObjectValue)fromValue), id, type);
                        m_symtab.insert(var, isStatic);

                        // put dest addr in memory
                        int into_addr_temp = m_symtab.getTemp(4);
                        var.putAddrInto(into_addr_temp, aw, m_symtab);

                        // load value into g1
                        aw.write("\t" + "set\t" + fromValue.getAssembly() + ", " + Register.G1);

                        if (from.getType().untypedef() instanceof IntType && type.untypedef() instanceof FloatType)
                        {
                            int temp = m_symtab.getTemp(4);
                            aw.storeLocal(Register.G1, temp);
                            aw.loadLocal(temp, Register.F0);
                            aw.write("\t" + "fitos\t" + Register.F0 + ", " + Register.F0);
                            aw.storeLocal(Register.F0, temp);
                            aw.loadLocal(temp, Register.G1);
                        }

                        // load dest addr into g2
                        aw.loadLocal(into_addr_temp, Register.G2);

                        // store g1 into [g2]
                        aw.write("\t" + "st\t" + Register.G1 + ", " + "[" + Register.G2 + "]");
                        aw.write("");

                    }
                } else {
                    m_nNumErrors++;
                    m_errors.print(Formatter.toString(ErrorMsg.error8_Assign, 
                                                      from.getType().getName(), 
                                                      ((Type)type).getName()));
                }

            } else if (pair.value instanceof ReportedErr) {
                m_symtab.insert(new ErrorSTO(pair.name));
            } else if (pair.value instanceof UnreportedErr) {
                m_symtab.insert(new ErrorSTO(pair.name));
                m_nNumErrors++;
                m_errors.print(Formatter.toString(ErrorMsg.error8_CompileTime, id));
            } else {
                assert false;
            }

        } // end pair loop

        if (isStatic && m_symtab.getFunc() != null) {
            aw.write(endLocalStaticInitLabel + ":");
            aw.write("");
        }

        isStatic = false;

    } // end DoConstDecl


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoTypedefDecl (Vector<String> lstIDs, TypeOrReportedErr maybeType)
	{

		for (int i = 0; i < lstIDs.size(); i++)
        {
            String id = lstIDs.elementAt(i);

            if (m_symtab.accessLocal(id) != null)
            {
                m_nNumErrors++;
                m_errors.print (Formatter.toString(ErrorMsg.redeclared_id, id));
            }
            
            if (maybeType instanceof ReportedErr) {
                m_symtab.insert(new ErrorSTO(id));
            } else if (maybeType instanceof Type) {
                Type type = (Type)maybeType;
                m_symtab.insert(new TypedefType(id, type));
            } else {
                assert false;
            }
        }
	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoStructdefDecl(String id, StructScope structScope)
	{
		if (m_symtab.accessLocal (id) != null)
        {
            m_nNumErrors++;
            m_errors.print (Formatter.toString(ErrorMsg.redeclared_id, id));
        }

        StructType type = new StructType(id, structScope);
		
		m_symtab.insert(type);
	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
    // return type will be reportedErr only if an error was returned
    // from doqualident, since the returnType param comes only from
    // the ReturnType grammar rule, which is an error only if SubType
    // is an error, which could be an error only if QualIdent is an
    // error, which could only have come from DoQualIdent.
    //
    // params comes from the grammar rule OptParamList, which is
    // filled with values from ParamDecl, which is an error only if
    // its Type was an error, which could be the case only if its
    // SubType or its OptArrayDef were errors.  SubType can be an
    // error only if qualident was an error.  OptArrayDef could have
    // an error only if it came from checkArrayDef, which could happen
    // only if

	void beginFuncDecl(TypeOrReportedErr maybeReturnType, String id, Vector<ParamDeclOrReportedErr> params)
	{
        aw = directWriter;

        boolean looksGood = true;

		if (m_symtab.checkStructMember(id) == true ||
            m_symtab.accessLocal(id) != null)
        {
            looksGood = false;
            m_nNumErrors++;
            if (m_symtab.inStructScope()) {
                m_errors.print(Formatter.toString(ErrorMsg.error13a_Struct, id));
            } else {
                m_errors.print (Formatter.toString(ErrorMsg.redeclared_id, id));
            }
        }

        if (maybeReturnType instanceof ReportedErr) {
            looksGood = false;
        }

        for (ParamDeclOrReportedErr pdore : params) {
            if (pdore instanceof ReportedErr) {
                looksGood = false;
            }
        }

        if (!looksGood) {
            m_symtab.insert(new ErrorSTO(id));
            return;
        }

        Vector<ParamDecl> goodParams = new Vector<ParamDecl>();
        for (ParamDeclOrReportedErr pd : params)
            goodParams.add((ParamDecl)pd);

        FunctionType type = new FunctionType((Type)maybeReturnType, goodParams);

        if (m_symtab.inStructScope()) {

            String structName = ((StructScope)m_symtab.getCurrentScope()).getType().untypedef().getName();

            UnboundFunction func = new UnboundFunction(id, type, structName);

            String fullName = func.getFullName();

            m_symtab.insert(func);
            m_symtab.openFunctionScope(func);

            // declare global name, write out the label, and save stack space
            aw.write("");
            aw.write("\t.global " + fullName);
            aw.write("\t.section\t\".text\"");
            aw.write(fullName + ":");
            aw.write("\tset\tSAVE." + fullName + ", " + Register.G1);
            aw.write("\tsave\t" + Register.SP + ", " + Register.G1 + ", " + Register.SP);
            aw.write("");

            // need to save the arguments
            aw.write("\t!\t" + "capying the arguments onto the stack");
            FunctionScope fs = (FunctionScope)m_symtab.getCurrentScope();
            aw.storeLocal(Register.I0, -68);
            for (int i = 0; i < params.size() && i < 5; ++i) {
                aw.storeLocal(Register.I[i+1], -(72 + (i*4)));
            }

        } else {

            NamedFunction func = new NamedFunction(id, type);

            m_symtab.insert(func);
            m_symtab.openFunctionScope(func);

            aw.write("");
            aw.write("\t.global " + id);
            aw.write("\t.section\t\".text\"");
            aw.write(id + ":");
            aw.write("\tset\tSAVE." + id + ", " + Register.G1);
            aw.write("\tsave\t" + Register.SP + ", " + Register.G1 + ", " + Register.SP);
            aw.write("");

            // update .lowest_sp_yet if appropriate
            String noUpdateLabel = LabelMaker.getLabel();

            aw.write("\t" + "set\t" + ".lowest_sp_yet, " + Register.G1);
            aw.write("\t" + "ld\t" + "[" + Register.G1 + "], " + Register.G2);
            aw.write("\t" + "cmp\t" + "%sp, " + Register.G2);
            aw.write("\t" + "bgeu\t" + noUpdateLabel);
            aw.write("\t" + "nop");

            // so we are doing the update
            aw.write("\t" + "st\t" + "%sp, " + "[" + Register.G1 + "]");
            aw.write(noUpdateLabel + ":\t! noUpdateLabel");
            
            // need to save the arguments
            aw.write("\t!\t" + "capying the arguments onto the stack");
            FunctionScope fs = (FunctionScope)m_symtab.getCurrentScope();
            for (int i = 0; i < params.size() && i < 6; ++i) {
                aw.storeLocal(Register.I[i], -(68 + (i*4)));
            }

            if (id.equals("main")) {

                // leave a space for __init_done
                aw.write("\t.section\t\".bss\"");
                aw.write("\t.align\t4");
                aw.write("__init_done:");
                aw.write("\t.skip\t4");

                // and back to the text segment
                aw.write("\t.section\t\".text\"");
                aw.write("\t.align\t4");

                // check whether init has already been called
                aw.write("\t" + "set\t" + "__init_done" + ", " + Register.L0);
                aw.write("\t" + "ld\t" + "[" + Register.L0 + "], "  + Register.L1);
                aw.write("\t" + "cmp\t" + Register.G0 + ", " + Register.L1);
                String alreadyInitedLabel = LabelMaker.getLabel();
                aw.write("\t" + "bne\t" + alreadyInitedLabel);
                aw.write("\t" + "nop");

                // setting up .lowest_sp_yet -- but only on the first run through
                aw.write("\t" + "set\t" + ".lowest_sp_yet, " + Register.G1);
                aw.write("\t" + "st\t" + "%sp, [" + Register.G1 + "]");

                aw.write("\t!\tCalling init function");
                aw.write("\tcall\t__init");
                aw.write("\tnop");

                // where to jump to if it already has
                aw.write(alreadyInitedLabel + ":");

                aw.write("");
            }
        }
	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoFuncDecl_2()
	{

        Expr func = (Expr) m_symtab.getFunc();
		FunctionScope fs = m_symtab.closeFunctionScope();
        
        assert func.getType() instanceof FunctionType;
        FunctionType ft = (FunctionType) func.getType();
        if ( !(ft.getReturnType() instanceof VoidType) && (!fs.getSeenReturn()))
        {
            m_nNumErrors++;
            m_errors.print(ErrorMsg.error6b_Return_missing);
        }

        aw.write("\tret");
        aw.write("\trestore");
        aw.write("");
        String id = fs.getName();
        aw.write("SAVE." + id + " = -(92 + " + fs.getStackSpace() + ") & -8");
        aw.write("");

        aw = initWriter;

	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoBlockOpen ()
	{
		m_symtab.openOtherScope();
	}


    void beginStruct(String name) {
        m_symtab.openStructScope(name);
    }

    StructScope endStruct() {
        StructScope retval = m_symtab.closeStructScope();


        aw.write(sizeName(retval.getName()) + " = " + retval.size());

        return retval;
    }


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	Scope DoBlockClose()
	{
		return m_symtab.closeScope();
	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	ExprOrReportedErr doAssignExpr(Expr into, Expr from)
	{
        /// \todo what is the original error message talked about in the assignment?

        DebugLogger.log("into type: " + into.getType());
        DebugLogger.log("from type: " + from.getType());

        if (!(into instanceof MLV)) {
            m_nNumErrors++;
            m_errors.print(ErrorMsg.error3a_Assign);
            return new ReportedErr();
        }
		
        /// \todo have i got all the typedef-related cases right?
        if (!(into.getType().isAssignableFrom(from.getType()))) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error3b_Assign, 
                                              from.getType().getName(),
                                              into.getType().getName()));
            return new ReportedErr();
        }

        return assign(into, from);


	}



	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	ExprOrReportedErr DoFuncCall(Expr expr, Vector<Expr> args)
	{

		if (!(expr.getEquivType() instanceof FunctionType))
        {
            m_nNumErrors++;
            if (expr instanceof STO) {
                m_errors.print(Formatter.toString(ErrorMsg.not_function, ((STO)expr).getName()));
            } else {
                /// \todo what should be printed in this case?
                m_errors.print(Formatter.toString(ErrorMsg.not_function, ""));
            }
            return new ReportedErr();
        }

        Callable lhs = (Callable) expr;

        FunctionType ft = lhs.getType();

        Vector<ParamDecl> params = ft.getParameters();

        if (params.size() != args.size()) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error5n_Call, args.size(), params.size()));
            return new ReportedErr();
        }

        boolean allClear = true;

        for (int i = 0; i < params.size(); ++i) {
            ParamDecl param = params.get(i);
            Expr arg = args.get(i);

            if (param.getIsRef()) {
                if (!(arg.getType().isTypedefEquivalent(param.getType()))) {
                    m_nNumErrors++;
                    m_errors.print(Formatter.toString(ErrorMsg.error5r_Call,
                                                      arg.getType().getName(),
                                                      param.getName(),
                                                      param.getType().getName()));
                    allClear = false;
                } else if (!(arg instanceof MLV) && !(arg.getType().topUntypedef() instanceof ArrayType)) {
                    m_nNumErrors++;
                    m_errors.print(Formatter.toString(ErrorMsg.error5c_Call,
                                                      param.getName(),
                                                      param.getType().getName()));
                    allClear = false;
                }

            } else {
                if (!(param.getType().isAssignableFrom(arg.getType()))) {
                    m_nNumErrors++;
                    m_errors.print(Formatter.toString(ErrorMsg.error5a_Call,
                                                      arg.getType().getName(),
                                                      param.getName(),
                                                      param.getType().getName()));
                    allClear = false;
                }
            }
        }
            
        if (!allClear) {
            return new ReportedErr();
        }

        return lhs.call(args, aw, m_symtab);

    }


    //----------------------------------------------------------------
    //
    //----------------------------------------------------------------
    ExprOrReportedErr DoDesignator2_Dot (Expr lhs, String strID)
    {
        aw.writeComment("Entering MyParser.DoDesignator2_Dot, id is " + strID);
        
        Type type = lhs.getType();
        Type realType = type.topUntypedef();
        if (!(realType instanceof StructType) &&
            !(realType instanceof IncompleteStructType))
        {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error14t_StructExp, type.getName()));
            return new ReportedErr();
        }

        ExprOrReportedErr retval;

        if (realType instanceof StructType) {
            retval =  lookInStruct(lhs, (StructType)realType, strID);
        } else if (realType instanceof IncompleteStructType) {
            retval = lookInStruct(lhs, (IncompleteStructType)realType, strID);
        } else {
            assert false;
            retval = null;
        }

        aw.writeComment("Leaving MyParser.DoDesignator2_Dot, returning " + retval);
        return retval;

    }


    //----------------------------------------------------------------
    //
    //----------------------------------------------------------------
    ExprOrReportedErr DoDesignator2_Array (Expr lhs, Expr rhs)
    {
        Type lhsType = lhs.getType();
        Type lhsRealType = lhs.getType().topUntypedef();

        /// \todo what if lhs is a function pointer?  this okay?
        if (!(lhsRealType instanceof ArrayType) &&
            !(lhsRealType instanceof PointerType))
        {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error11t_ArrExp,
                                              lhsType.getName()));
            return new ReportedErr();
        }

        if (!(rhs.getType().isTypedefEquivalent(new IntType())))
        {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error11i_ArrExp,
                                              rhs.getType().getName()));
            return new ReportedErr();
        }

        if (lhsRealType instanceof ArrayType && rhs instanceof ConstExpr)
        {
            ConstExpr constRhs = (ConstExpr)rhs;
            Value val = constRhs.getValue();
            assert val instanceof IntValue;
            int index = ((IntValue)val).getValue();
            
            ArrayType array = (ArrayType)lhsRealType;

            if (index < 0 || index >= array.getNumElements())
            {
                m_nNumErrors++;
                m_errors.print(Formatter.toString(ErrorMsg.error11b_ArrExp,
                                                  index,
                                                  array.getNumElements()));
                return new ReportedErr();
            }
        }

        Type baseType;

        // runtime checks
        if (lhsRealType instanceof ArrayType) {
            baseType = ((ArrayType)lhsRealType).getBaseType().untypedef();

            // do bounds check

            String noProblemLabel = LabelMaker.getLabel();
            String problemLabel = LabelMaker.getLabel();

            int arrayElementCount = ((ArrayType)(lhs.getEquivType())).getNumElements();
            
            int rhsValueTemp = m_symtab.getTemp(4);
            rhs.putValueInto(rhsValueTemp, aw, m_symtab);

            aw.write("\t" + "set\t" + arrayElementCount + ", " + Register.L0);
            aw.loadLocal(rhsValueTemp, Register.L1);

            aw.write("\t" + "cmp\t" + Register.L1 + ", " + Register.L0);
            aw.write("\t" + "bge\t" + problemLabel);
            aw.write("\t" + "nop");

            aw.write("\t" + "cmp\t" + Register.L1 + ", " + Register.G0);
            aw.write("\t" + "bge\t" + noProblemLabel);
            aw.write("\t" + "nop");

            aw.write(problemLabel + ":");
            aw.write("\t" + "mov\t" + Register.L1 + ", " + Register.O1);
            aw.write("\t" + "mov\t" + Register.L0 + ", " + Register.O2);
            aw.write("\t" + "call\t" + "__bad_array_access");
            aw.write("\t" + "nop");
            

            aw.write(noProblemLabel + ":");

        } else if (lhsRealType instanceof PointerType) {
            baseType = ((PointerType)lhsRealType).getPointee().untypedef();

            emitPointerCheck(lhs, Register.L0, Register.L1);
        } else {
            assert false;
            baseType = null;
        }

        if (baseType instanceof FunctionType) {
            return new FuncPtrFromArrayDeref((FunctionType)baseType, lhs, rhs);
        } else {
            ArrayDeref retval = new ArrayDeref(lhs, rhs);
            return retval;
        }

    }


    //----------------------------------------------------------------
    //
    //----------------------------------------------------------------
    ExprOrReportedErr DoDesignator3_ID(String strID)
    {
        STO sto = m_symtab.access(strID);

        if (sto == null) {
            m_nNumErrors++;
            m_errors.print (Formatter.toString(ErrorMsg.undeclared_id, strID));	
            return new ReportedErr(strID);
        }

        if (sto instanceof Expr) {
            return (Expr)sto;
        } else if (sto instanceof ErrorSTO) {
            return new ReportedErr();
        } else {
            assert false;
            return null;
        }
    }


    //----------------------------------------------------------------
    //
    //----------------------------------------------------------------
    TypeOrReportedErr DoQualIdent(String id)
    {
        STO sto = m_symtab.access(id);

        if (sto == null) {
            m_nNumErrors++;
            m_errors.print (Formatter.toString(ErrorMsg.undeclared_id, id));
            return (new ReportedErr(id));
        }

        if (sto instanceof ErrorSTO) {
            return new ReportedErr();
        } else if (!(sto instanceof Type)) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.not_type, sto.getName()));
            return new ReportedErr(sto.getName());
        } else {
            return (Type)sto;
        }
    }

    ExprOrReportedErr doAddMulOp(Expr lhs, Operator op, Expr rhs) {
        /// \todo if lhs or rhs is a typedef type, the error message
        /// is printing the base type instead of the typedef name.

        assert op == Operator.PLUS || op == Operator.MINUS || op == Operator.STAR || op == Operator.SLASH;

        if (!(lhs.getEquivType() instanceof NumericType))
        {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error1n_Expr, lhs.getType().getName(), op.toString()));
            return new ReportedErr();
        }

        if (!(rhs.getEquivType() instanceof NumericType)) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error1n_Expr, rhs.getType().getName(), op.toString()));
            return new ReportedErr();
        }

        NCRV retval;

        if (lhs instanceof ConstExpr && rhs instanceof ConstExpr)
        {
            NumericValue lhsValue = (NumericValue)((ConstExpr)lhs).getValue();
            NumericValue rhsValue = (NumericValue)((ConstExpr)rhs).getValue();

            try {
                return new CRV(lhsValue.addMul(op, rhsValue));
            } catch (ArithmeticException e) {
                /// \todo i should be getting a redefinition error
                /// after a failed initialization of a constant that
                /// triggered this.
                m_nNumErrors++;
                m_errors.print(ErrorMsg.error8_Arithmetic);
                return new ReportedErr();
            }
        } else {
            if (lhs.getEquivType() instanceof IntType && rhs.getEquivType() instanceof IntType) {

                retval = new NCRV(new IntType());
                int lhs_val_temp = m_symtab.getTemp(4);
                lhs.putValueInto(lhs_val_temp, aw, m_symtab);

                int rhs_val_temp = m_symtab.getTemp(4);
                rhs.putValueInto(rhs_val_temp, aw, m_symtab);

                aw.loadLocal(lhs_val_temp, Register.O0);
                aw.loadLocal(rhs_val_temp, Register.O1);

                switch (op) {
                case PLUS:
                    aw.write("\t" + "add\t" + Register.O0 + ", " + Register.O1 + ", " + Register.O0);
                    break;
                case MINUS:
                    aw.write("\t" + "sub\t" + Register.O0 + ", " + Register.O1 + ", " + Register.O0);
                    break;
                case STAR:
                    aw.write("\t" + "call\t.mul");
                    aw.write("\tnop");
                    break;
                case SLASH:
                    aw.write("\t" + "call\t.div");
                    aw.write("\tnop");
                    break;
                default:
                    assert false;
                }
                // the value is now in O0
                // and save the result to some temp
                int this_val_temp = m_symtab.getTemp(4);
                retval.setLocation(this_val_temp);
                aw.storeLocal(Register.O0, this_val_temp);

                return retval;

            } else {
                // at least one float

                assert lhs.getEquivType() instanceof FloatType || rhs.getEquivType() instanceof FloatType;
                retval = new NCRV(new FloatType());

                int lhs_val_temp = m_symtab.getTemp(4);
                lhs.putValueInto(lhs_val_temp, aw, m_symtab);

                int rhs_val_temp = m_symtab.getTemp(4);
                rhs.putValueInto(rhs_val_temp, aw, m_symtab);

                aw.loadLocal(lhs_val_temp, Register.F0);
                aw.loadLocal(rhs_val_temp, Register.F1);

                if (lhs.getEquivType() instanceof IntType)
                    aw.write("\t" + "fitos" + "\t" + Register.F0 + ", " + Register.F0);
                
                if (rhs.getEquivType() instanceof IntType)
                    aw.write("\t" + "fitos" + "\t" + Register.F1 + ", " + Register.F1);

                switch (op) {

                case PLUS:
                    aw.write("\t" + "fadds\t" + Register.F0 + ", " + Register.F1 + ", " + Register.F0);
                    break;
                case MINUS:
                    aw.write("\t" + "fsubs\t" + Register.F0 + ", " + Register.F1 + ", " + Register.F0);
                    break;
                case STAR:
                    aw.write("\t" + "fmuls\t" + Register.F0 + ", " + Register.F1 + ", " + Register.F0);
                    break;
                case SLASH:
                    aw.write("\t" + "fdivs\t" + Register.F0 + ", " + Register.F1 + ", " + Register.F0);
                    break;
                default:
                    assert false;
                }
                
                int this_val_temp = m_symtab.getTemp(4);
                retval.setLocation(this_val_temp);
                aw.storeLocal(Register.F0, this_val_temp);                

                return retval;

            }


        }
    }

    ExprOrReportedErr doMod(Expr lhs, Operator op, Expr rhs) {
        assert op == Operator.MOD;
        if (!(lhs.getEquivType() instanceof IntType))
        {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error1w_Expr,
                                              lhs.getType().getName(),
                                              op.toString(),
                                              (new IntType()).getName()));
            return new ReportedErr();
        }

        if (!(rhs.getEquivType() instanceof IntType)) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error1w_Expr, 
                                              rhs.getType().getName(),
                                              op.toString(),
                                              (new IntType()).getName()));
            return new ReportedErr();
        }
        
        if (lhs instanceof ConstExpr && rhs instanceof ConstExpr)
        {
            IntValue lhsValue = (IntValue)((ConstExpr)lhs).getValue();
            IntValue rhsValue = (IntValue)((ConstExpr)rhs).getValue();

            return new CRV(new IntValue(lhsValue.getValue() % rhsValue.getValue()));
        } else {
            NCRV retval = new NCRV(new IntType());

            int lhs_val_temp = m_symtab.getTemp(4);
            lhs.putValueInto(lhs_val_temp, aw, m_symtab);

            int rhs_val_temp = m_symtab.getTemp(4);
            rhs.putValueInto(rhs_val_temp, aw, m_symtab);

            aw.loadLocal(lhs_val_temp, Register.O0);
            aw.loadLocal(rhs_val_temp, Register.O1);
            aw.write("\t" + "call" + "\t" + ".rem");
            aw.write("\t" + "nop");
            
            int this_val_temp = m_symtab.getTemp(4);
            retval.setLocation(this_val_temp);
            aw.storeLocal(Register.O0, this_val_temp);

            return retval;
        }
    }

    ExprOrReportedErr doRelationOp(Expr lhs, Operator op, Expr rhs) {
        assert op == Operator.LT || op == Operator.LTE || op == Operator.GT || op == Operator.GTE;

        if (!(lhs.getEquivType() instanceof NumericType))
        {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error1n_Expr, lhs.getType().getName(), op.toString()));
            return new ReportedErr();
        }

        if (!(rhs.getEquivType() instanceof NumericType)) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error1n_Expr, rhs.getType().getName(), op.toString()));
            return new ReportedErr();
        }

        if (lhs instanceof ConstExpr && rhs instanceof ConstExpr)
        {
            NumericValue lhsValue = (NumericValue)((ConstExpr)lhs).getValue();
            NumericValue rhsValue = (NumericValue)((ConstExpr)rhs).getValue();

            return new CRV(lhsValue.relation(op, rhsValue));
        } else {
            
            NCRV retval = new NCRV(new BooleanType());


            if (lhs.getEquivType() instanceof IntType && rhs.getEquivType() instanceof IntType) {

                int lhs_val_temp = m_symtab.getTemp(4);
                lhs.putValueInto(lhs_val_temp, aw, m_symtab);

                int rhs_val_temp = m_symtab.getTemp(4);
                rhs.putValueInto(rhs_val_temp, aw, m_symtab);

                aw.loadLocal(lhs_val_temp, Register.O0);
                aw.loadLocal(rhs_val_temp, Register.O1);
                aw.write("\t" + "cmp\t" + Register.O0 + ", " + Register.O1);

                String branchCode;
                switch (op) {
                case LT: branchCode = "bl"; break;
                case LTE: branchCode = "ble"; break;
                case GT: branchCode = "bg"; break;
                case GTE: branchCode = "bge"; break;
                default: branchCode = ""; assert false;
                }

                String trueLabel = LabelMaker.getLabel();
                String doneLabel = LabelMaker.getLabel();
            
                aw.write("\t" + branchCode + "\t" + trueLabel);
                aw.write("\tnop");
                aw.write("\tmov\t0, " + Register.O0);
                aw.write("\tba\t" + doneLabel);
                aw.write("\tnop");

                aw.write(trueLabel + ":");
                aw.write("\tmov\t1, " + Register.O0);
                aw.write(doneLabel + ":");

                // the value is now in O0
                // and save the result to some temp
                int this_val_temp = m_symtab.getTemp(4);
                retval.setLocation(this_val_temp);
                aw.storeLocal(Register.O0, this_val_temp);
            
            } else {
                // at least one arg is a float, so we have to convert both to float

                int lhs_val_temp = m_symtab.getTemp(4);
                lhs.putValueInto(lhs_val_temp, aw, m_symtab);

                int rhs_val_temp = m_symtab.getTemp(4);
                rhs.putValueInto(rhs_val_temp, aw, m_symtab);

                aw.loadLocal(lhs_val_temp, Register.F0);
                aw.loadLocal(rhs_val_temp, Register.F1);

                if (lhs.getEquivType() instanceof IntType)
                    aw.write("\t" + "fitos" + "\t" + Register.F0 + ", " + Register.F0);
                
                if (rhs.getEquivType() instanceof IntType)
                    aw.write("\t" + "fitos" + "\t" + Register.F1 + ", " + Register.F1);
                
                aw.write("\t" + "fcmps" + "\t" + Register.F0 + ", " + Register.F1);
                aw.write("\t" + "nop");

                String branchCode;
                switch (op) {
                case LT: branchCode = "fbl"; break;
                case LTE: branchCode = "fble"; break;
                case GT: branchCode = "fbg"; break;
                case GTE: branchCode = "fbge"; break;
                default: branchCode = ""; assert false;
                }

                String trueLabel = LabelMaker.getLabel();
                String doneLabel = LabelMaker.getLabel();

                aw.write("\t" + branchCode + "\t" + trueLabel);
                aw.write("\tnop");

                aw.write("\tmov\t0, " + Register.O0);
                aw.write("\tba\t" + doneLabel);
                aw.write("\tnop");

                aw.write(trueLabel + ":");
                aw.write("\tmov\t1, " + Register.O0);
                aw.write(doneLabel + ":");

                int this_val_temp = m_symtab.getTemp(4);
                retval.setLocation(this_val_temp);
                aw.storeLocal(Register.O0, this_val_temp);

            }

            return retval;
        }
    }

    ExprOrReportedErr doEqualityOp(Expr lhs, Operator op, Expr rhs) {
        assert op == Operator.EQU || op == Operator.NEQ;

        if (lhs instanceof AbstractFunctionPointer && rhs.getEquivType() instanceof NullType) {
            AbstractFunctionPointer fp = (AbstractFunctionPointer) lhs;
            return emitFunctionPointerNullComparison(op, fp);

        } else if (lhs.getEquivType() instanceof NullType && rhs instanceof AbstractFunctionPointer) {
            AbstractFunctionPointer fp = (AbstractFunctionPointer) rhs;
            return emitFunctionPointerNullComparison(op, fp);

        } else if (lhs.getEquivType() instanceof PointerType || 
            lhs.getEquivType() instanceof NullType ||
            rhs.getEquivType() instanceof PointerType || 
            rhs.getEquivType() instanceof NullType)
        {

            if (lhs.getEquivType() instanceof NullType && rhs.getEquivType() instanceof NullType) {
                if (op == Operator.EQU) {
                    return new CRV(new BooleanValue(true));
                } else if (op == Operator.NEQ) {
                    return new CRV(new BooleanValue(false));
                } else {
                    assert false;
                    return null;
                }

            } else if ((lhs.getEquivType() instanceof NullType && rhs.getEquivType() instanceof PointerType) ||
                       (lhs.getEquivType() instanceof PointerType && rhs.getEquivType() instanceof NullType) ||
                       (lhs.getType().isTypedefEquivalent(rhs.getType())))
            {


                NCRV retval = new NCRV(new BooleanType());

                aw.write("\t!\t" + "Doing equality operator on two pointers.");

                int lhs_val_temp = m_symtab.getTemp(4);
                lhs.putValueInto(lhs_val_temp, aw, m_symtab);

                int rhs_val_temp = m_symtab.getTemp(4);
                rhs.putValueInto(rhs_val_temp, aw, m_symtab);

                aw.loadLocal(lhs_val_temp, Register.O0);
                aw.loadLocal(rhs_val_temp, Register.O1);
                aw.write("\t" + "cmp\t" + Register.O0 + ", " + Register.O1);

                String branchCode;
                switch (op) {
                case EQU: branchCode = "be"; break;
                case NEQ: branchCode = "bne"; break;
                default: branchCode = ""; assert false;
                }

                String trueLabel = LabelMaker.getLabel();
                String doneLabel = LabelMaker.getLabel();
            
                aw.write("\t" + branchCode + "\t" + trueLabel);
                aw.write("\tnop");
                aw.write("\tmov\t0, " + Register.O0);
                aw.write("\tba\t" + doneLabel);
                aw.write("\tnop");

                aw.write(trueLabel + ":");
                aw.write("\tmov\t1, " + Register.O0);
                aw.write(doneLabel + ":");

                // the value is now in O0
                // and save the result to some temp
                int this_val_temp = m_symtab.getTemp(4);
                retval.setLocation(this_val_temp);
                aw.storeLocal(Register.O0, this_val_temp);


                return retval;

            } else {
                m_nNumErrors++;
                m_errors.print(Formatter.toString(ErrorMsg.error17_Expr, 
                                                  op.toString(), 
                                                  lhs.getType().getName(),
                                                  rhs.getType().getName()));
                return new ReportedErr();
            }
        } else if (lhs.getEquivType() instanceof NumericType) {
            if (!(rhs.getEquivType() instanceof NumericType)) {
                m_nNumErrors++;
                m_errors.print(Formatter.toString(ErrorMsg.error1b_Expr, lhs.getType().getName(),
                                                  op.toString(), rhs.getType().getName()));
                return new ReportedErr();
            } 

            // both are numeric
            if (lhs instanceof ConstExpr && rhs instanceof ConstExpr) {
                NumericValue lhsValue = (NumericValue)((ConstExpr)lhs).getValue();
                NumericValue rhsValue = (NumericValue)((ConstExpr)rhs).getValue();
                return new CRV(lhsValue.equality(op, rhsValue));
            } else {
                // doEqualityOp, both numeric, result nonconst

                NCRV retval = new NCRV(new BooleanType());

                if (lhs.getEquivType() instanceof IntType && rhs.getEquivType() instanceof IntType) {
                    // do equality op, both int, result nonconst, 

                    aw.write("\t!\t" + "Doing equality operator on two ints");

                    int lhs_val_temp = m_symtab.getTemp(4);
                    lhs.putValueInto(lhs_val_temp, aw, m_symtab);

                    int rhs_val_temp = m_symtab.getTemp(4);
                    rhs.putValueInto(rhs_val_temp, aw, m_symtab);

                    aw.loadLocal(lhs_val_temp, Register.O0);
                    aw.loadLocal(rhs_val_temp, Register.O1);
                    aw.write("\t" + "cmp\t" + Register.O0 + ", " + Register.O1);

                    String branchCode;
                    switch (op) {
                    case EQU: branchCode = "be"; break;
                    case NEQ: branchCode = "bne"; break;
                    default: branchCode = ""; assert false;
                    }

                    String trueLabel = LabelMaker.getLabel();
                    String doneLabel = LabelMaker.getLabel();
            
                    aw.write("\t" + branchCode + "\t" + trueLabel);
                    aw.write("\tnop");
                    aw.write("\tmov\t0, " + Register.O0);
                    aw.write("\tba\t" + doneLabel);
                    aw.write("\tnop");

                    aw.write(trueLabel + ":");
                    aw.write("\tmov\t1, " + Register.O0);
                    aw.write(doneLabel + ":");

                    // the value is now in O0
                    // and save the result to some temp
                    int this_val_temp = m_symtab.getTemp(4);
                    retval.setLocation(this_val_temp);
                    aw.storeLocal(Register.O0, this_val_temp);
            
                } else {
                    // do equality op, both numeric, one or more float, result nonconst float
                    // at least one arg is a float, so we have to convert both to float

                    aw.write("\t!\t" + "Doing equality operator on numeric types, one of which is not an int");

                    int lhs_val_temp = m_symtab.getTemp(4);
                    lhs.putValueInto(lhs_val_temp, aw, m_symtab);

                    int rhs_val_temp = m_symtab.getTemp(4);
                    rhs.putValueInto(rhs_val_temp, aw, m_symtab);

                    aw.loadLocal(lhs_val_temp, Register.F0);
                    aw.loadLocal(rhs_val_temp, Register.F1);

                    if (lhs.getEquivType() instanceof IntType)
                        aw.write("\t" + "fitos" + "\t" + Register.F0 + ", " + Register.F0);
                
                    if (rhs.getEquivType() instanceof IntType)
                        aw.write("\t" + "fitos" + "\t" + Register.F1 + ", " + Register.F1);
                
                    aw.write("\t" + "fcmps" + "\t" + Register.F0 + ", " + Register.F1);
                    aw.write("\t" + "nop");

                    String branchCode;
                    switch (op) {
                    case EQU: branchCode = "fbe"; break;
                    case NEQ: branchCode = "fbne"; break;
                    default: branchCode = ""; assert false;
                    }

                    String trueLabel = LabelMaker.getLabel();
                    String doneLabel = LabelMaker.getLabel();

                    aw.write("\t" + branchCode + "\t" + trueLabel);
                    aw.write("\tnop");

                    aw.write("\tmov\t0, " + Register.O0);
                    aw.write("\tba\t" + doneLabel);
                    aw.write("\tnop");

                    aw.write(trueLabel + ":");
                    aw.write("\tmov\t1, " + Register.O0);
                    aw.write(doneLabel + ":");

                    int this_val_temp = m_symtab.getTemp(4);
                    retval.setLocation(this_val_temp);
                    aw.storeLocal(Register.O0, this_val_temp);
                }

                return retval;
            }

        } else if (lhs.getEquivType() instanceof BooleanType) {
            if (!(rhs.getEquivType() instanceof BooleanType)) {
                m_nNumErrors++;
                m_errors.print(Formatter.toString(ErrorMsg.error1b_Expr, lhs.getType().getName(),
                                                  op.toString(), rhs.getType().getName()));
                return new ReportedErr();
            } 

            // both are boolean
            if (lhs instanceof ConstExpr && rhs instanceof ConstExpr) {
                boolean l = ((BooleanValue)((ConstExpr)lhs).getValue()).getValue();
                boolean r = ((BooleanValue)((ConstExpr)rhs).getValue()).getValue();
                if (op == Operator.EQU)
                    return new CRV(new BooleanValue(l == r));
                else if (op == Operator.NEQ)
                    return new CRV(new BooleanValue(l != r));
            } else {
                // equality operator, at least one nonconst

                aw.write("\t!\t" + "Doing equality operator on two bools");

                NCRV retval = new NCRV(new BooleanType());

                int lhs_val_temp = m_symtab.getTemp(4);
                lhs.putValueInto(lhs_val_temp, aw, m_symtab);

                int rhs_val_temp = m_symtab.getTemp(4);
                rhs.putValueInto(rhs_val_temp, aw, m_symtab);

                aw.loadLocal(lhs_val_temp, Register.O0);
                aw.loadLocal(rhs_val_temp, Register.O1);

                // normalize %o0
                String o0_is_true = LabelMaker.getLabel();
                aw.write("\t" + "cmp\t" + Register.O0 + ", " + Register.G0);
                aw.write("\t" + "be" + "\t" + o0_is_true);
                aw.write("\t" + "nop");
                aw.write("\t" + "mov\t1, " + Register.O0);
                aw.write(o0_is_true + ":");

                // normalize %01
                String o1_is_true = LabelMaker.getLabel();
                aw.write("\t" + "cmp\t" + Register.O1 + ", " + Register.G0);
                aw.write("\t" + "be" + "\t" + o1_is_true);
                aw.write("\t" + "nop");
                aw.write("\t" + "mov\t1, " + Register.O1);
                aw.write(o1_is_true + ":");

                aw.write("\t" + "cmp\t" + Register.O0 + ", " + Register.O1);

                String branchCode;
                switch (op) {
                case EQU: branchCode = "be"; break;
                case NEQ: branchCode = "bne"; break;
                default: branchCode = ""; assert false;
                }

                String trueLabel = LabelMaker.getLabel();
                String doneLabel = LabelMaker.getLabel();

                aw.write("\t" + branchCode + "\t" + trueLabel);
                aw.write("\tnop");
                aw.write("\tmov\t0, " + Register.O0);
                aw.write("\tba\t" + doneLabel);
                aw.write("\tnop");

                aw.write(trueLabel + ":");
                aw.write("\tmov\t1, " + Register.O0);
                aw.write(doneLabel + ":");

                // the value is now in O0
                // and save the result to some temp
                int this_val_temp = m_symtab.getTemp(4);
                retval.setLocation(this_val_temp);
                aw.storeLocal(Register.O0, this_val_temp);

                return retval;
            }

        } else {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error1b_Expr, lhs.getType().getName(),
                                              op.toString(), rhs.getType().getName()));
            return new ReportedErr();
        }

        assert false;
        return null;
    }

    void doHalfAnd(Expr lhs) {
        String returnFalseLabel = LabelMaker.getLabel();
        andLabels.push(returnFalseLabel);

        if (lhs instanceof ConstExpr) {
            if ( ( (BooleanValue)((ConstExpr)lhs).getValue()).getValue() == false) {
                aw.write("\t" + "ba" + "\t" + returnFalseLabel);
                aw.write("\t" + "nop");
            }
        } else {

            aw.write("\t!\t" + "doHalfAnd");
        
            // if lhs is false, skip ahead to return false

            int lhs_val_temp = m_symtab.getTemp(4);
            lhs.putValueInto(lhs_val_temp, aw, m_symtab);
            aw.loadLocal(lhs_val_temp, Register.O0);
            aw.write("\t" + "cmp" + "\t" + Register.O0 + ", " + Register.G0);
            aw.write("\t" + "be" + "\t" + returnFalseLabel);
            aw.write("\t" + "nop");
        }
    }

    ExprOrReportedErr doAnd(Expr lhs, Expr rhs) {

        String returnFalseLabel = andLabels.pop();

        if (!(lhs.getEquivType() instanceof BooleanType)) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error1w_Expr, lhs.getType().getName(),
                                              Operator.AND.toString(), (new BooleanType()).getName()));
            return new ReportedErr();
        }
        
        if (!(rhs.getEquivType() instanceof BooleanType)) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error1w_Expr, rhs.getType().getName(),
                                              Operator.AND.toString(), (new BooleanType()).getName()));
            return new ReportedErr();
        }
        
        if (lhs instanceof ConstExpr && rhs instanceof ConstExpr) {
            boolean l = ((BooleanValue)((ConstExpr)lhs).getValue()).getValue();
            boolean r = ((BooleanValue)((ConstExpr)rhs).getValue()).getValue();
            aw.write(returnFalseLabel + ":");
            return new CRV(new BooleanValue(l && r));
        } else {

            NCRV retval = new NCRV(new BooleanType());

            aw.write("\t!\t" + "doing and");

            String doneLabel = LabelMaker.getLabel();

            if (lhs instanceof ConstExpr && ( ( (BooleanValue)((ConstExpr)lhs).getValue()).getValue() == false) ) {
                // then we've already jumped ahead to returnFalseLabel
            } else {
                // if lhs was false, we will be skipping over this code
                // still here, so test rhs
                int rhs_val_temp = m_symtab.getTemp(4);
                rhs.putValueInto(rhs_val_temp, aw, m_symtab);
                aw.loadLocal(rhs_val_temp, Register.O0);
                aw.write("\t" + "cmp" + "\t" + Register.O0 + ", " + Register.G0);
                aw.write("\t" + "be" + "\t" + returnFalseLabel);
                aw.write("\t" + "nop");

                // still here, so both were true
                aw.write("\t" + "mov" + "\t" + 1 + ", " + Register.O0);
                aw.write("\t" + "ba" + "\t" + doneLabel);
                aw.write("\t" + "nop");
            }

            // the return false branch
            aw.write(returnFalseLabel + ":" + "\t!\t" + "returnFalseLabel");
            aw.write("\t" + "mov" + "\t" + 0 + ", " + Register.O0);

            // all done
            aw.write(doneLabel + ":" + "\t!\t" + "doneLabel");


            // bookkeeping
            int this_val_temp = m_symtab.getTemp(4);
            retval.setLocation(this_val_temp);
            aw.storeLocal(Register.O0, this_val_temp);
        
            return retval;
        }
    }


    void doHalfOr(Expr lhs) {
        String returnTrueLabel = LabelMaker.getLabel();
        orLabels.push(returnTrueLabel);

        if (lhs instanceof ConstExpr) {
            if ( ( (BooleanValue)((ConstExpr)lhs).getValue()).getValue() == true) {
                aw.write("\t" + "ba" + "\t" + returnTrueLabel);
                aw.write("\t" + "nop");
            }
        } else { 
            aw.write("\t!\t" + "doHalfOr");
        
            // if lhs is true, skip ahead to return true

            int lhs_val_temp = m_symtab.getTemp(4);
            lhs.putValueInto(lhs_val_temp, aw, m_symtab);
            aw.loadLocal(lhs_val_temp, Register.O0);
            aw.write("\t" + "cmp" + "\t" + Register.O0 + ", " + Register.G0);
            aw.write("\t" + "bne" + "\t" + returnTrueLabel);
            aw.write("\t" + "nop");
        }
    }

    ExprOrReportedErr doOr(Expr lhs, Expr rhs) {
        String returnTrueLabel = orLabels.pop();

        if (!(lhs.getEquivType() instanceof BooleanType)) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error1w_Expr, lhs.getType().getName(),
                                              Operator.OR.toString(), (new BooleanType()).getName()));
            return new ReportedErr();
        }
        
        if (!(rhs.getEquivType() instanceof BooleanType)) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error1w_Expr, rhs.getType().getName(),
                                              Operator.OR.toString(), (new BooleanType()).getName()));
            return new ReportedErr();
        }
        
        if (lhs instanceof ConstExpr && rhs instanceof ConstExpr) {
            boolean l = ((BooleanValue)((ConstExpr)lhs).getValue()).getValue();
            boolean r = ((BooleanValue)((ConstExpr)rhs).getValue()).getValue();
            aw.write(returnTrueLabel + ":");
            return new CRV(new BooleanValue(l || r));
        } else {

            NCRV retval = new NCRV(new BooleanType());
            
            aw.write("\t!\t" + "doing or");

            String doneLabel = LabelMaker.getLabel();

            if (lhs instanceof ConstExpr && ( ( (BooleanValue)((ConstExpr)lhs).getValue()).getValue() == true) ) {
                // then we've already branched ahead to returnTrueLabel
            } else {
                // if lhs was true, we will be skipping over this code
                // still here, so test rhs
                int rhs_val_temp = m_symtab.getTemp(4);
                rhs.putValueInto(rhs_val_temp, aw, m_symtab);
                aw.loadLocal(rhs_val_temp, Register.O0);
                aw.write("\t" + "cmp" + "\t" + Register.O0 + ", " + Register.G0);
                aw.write("\t" + "bne" + "\t" + returnTrueLabel);
                aw.write("\t" + "nop");

                // still here, so both were false
                aw.write("\t" + "mov" + "\t" + 0 + ", " + Register.O0);
                aw.write("\t" + "ba" + "\t" + doneLabel);
                aw.write("\t" + "nop");
            }

            // the return true branch
            aw.write(returnTrueLabel + ":" + "\t!\t" + "returnTrueLabel");
            aw.write("\t" + "mov" + "\t" + 1 + ", " + Register.O0);

            // all done
            aw.write(doneLabel + ":" + "\t!\t" + "doneLabel");


            // bookkeeping
            int this_val_temp = m_symtab.getTemp(4);
            retval.setLocation(this_val_temp);
            aw.storeLocal(Register.O0, this_val_temp);
                
            return retval;
        }
    }



    ExprOrReportedErr doNot(Expr rhs) {
        if (!(rhs.getEquivType() instanceof BooleanType)) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error1u_Expr, rhs.getType().getName(),
                                              Operator.NOT.toString(), (new BooleanType()).getName()));
            return new ReportedErr();
        }

        if (rhs instanceof ConstExpr) {
            boolean r = ((BooleanValue)((ConstExpr)rhs).getValue()).getValue();
            return new CRV(new BooleanValue(!r));
        } else {
            NCRV retval =  new NCRV(new BooleanType());

            aw.write("\t!\t" + "Doing not");

            int rhs_val_temp = m_symtab.getTemp(4);
            rhs.putValueInto(rhs_val_temp, aw, m_symtab);

            aw.loadLocal(rhs_val_temp, Register.O0);

            String returnTrueLabel = LabelMaker.getLabel();
            String doneLabel = LabelMaker.getLabel();

            aw.write("\t" + "cmp" + "\t" + Register.O0 + ", " + Register.G0);
            aw.write("\t" + "be" + "\t" + returnTrueLabel);
            aw.write("\t" + "nop");
            aw.write("\t" + "mov" + "\t" + 0 + ", " + Register.O0);
            aw.write("\t" + "ba" + "\t" + doneLabel);
            aw.write("\t" + "nop");
            aw.write(returnTrueLabel + ":" + "\t!\t" + "returnTrueLabel");
            aw.write("\t" + "mov" + "\t" + 1 + ", " + Register.O0);
            aw.write(doneLabel + ":" + "\t!\t" + "doneLabel");

            int this_val_temp = m_symtab.getTemp(4);
            retval.setLocation(this_val_temp);
            aw.storeLocal(Register.O0, this_val_temp);

            return retval;
        }

    }

    ExprOrReportedErr doBCA(Expr lhs, Operator op, Expr rhs) {
        assert op == Operator.BAR || op == Operator.CARET || op == Operator.AMPERSAND;

        if (!(lhs.getEquivType() instanceof IntType)) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error1w_Expr, lhs.getType().getName(),
                                              op.toString(), (new IntType()).getName()));
            return new ReportedErr();
        }
        
        if (!(rhs.getEquivType() instanceof IntType)) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error1w_Expr, rhs.getType().getName(),
                                              op.toString(), (new IntType()).getName()));
            return new ReportedErr();
        }
        
        if (lhs instanceof ConstExpr && rhs instanceof ConstExpr) {
            int l = ((IntValue)((ConstExpr)lhs).getValue()).getValue();
            int r = ((IntValue)((ConstExpr)rhs).getValue()).getValue();
            if (op == Operator.BAR) {
                return new CRV(new IntValue(l | r));
            } else if (op == Operator.CARET) {
                return new CRV(new IntValue(l ^ r));
            } else {
                assert op == Operator.AMPERSAND;
                return new CRV(new IntValue(l & r));
            }
        } else {
            NCRV retval = new NCRV(new IntType());

            int lhs_val_temp = m_symtab.getTemp(4);
            lhs.putValueInto(lhs_val_temp, aw, m_symtab);

            int rhs_val_temp = m_symtab.getTemp(4);
            rhs.putValueInto(rhs_val_temp, aw, m_symtab);

            aw.loadLocal(lhs_val_temp, Register.O0);
            aw.loadLocal(rhs_val_temp, Register.O1);

            switch (op) {
            case BAR:
                aw.write("\t" + "or\t" + Register.O0 + ", " + Register.O1 + ", " + Register.O0);
                break;
            case CARET:
                aw.write("\t" + "xor\t" + Register.O0 + ", " + Register.O1 + ", " + Register.O0);
                break;
            case AMPERSAND:
                aw.write("\t" + "and\t" + Register.O0 + ", " + Register.O1 + ", " + Register.O0);
                break;
            default:
                assert false;
            }
            // the value is now in O0
            // and save the result to some temp
            int this_val_temp = m_symtab.getTemp(4);
            retval.setLocation(this_val_temp);
            aw.storeLocal(Register.O0, this_val_temp);

            return retval;
        }
        
    }

    ExprOrReportedErr doPreIncDecOp(Operator op, Expr rhs) {
        assert op == Operator.PLUSPLUS || op == Operator.MINUSMINUS;

        if (!(rhs.getEquivType() instanceof NumericType) &&
            !( (rhs.getEquivType() instanceof PointerType) && !(  ((PointerType)rhs.getEquivType()).getPointee().untypedef() instanceof FunctionType))  ) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error2_Type, rhs.getType().getName(), op.toString()));
            return new ReportedErr();
        }

        if (!(rhs instanceof MLV)) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error2_Lval, op.toString()));
            return new ReportedErr();
        }

        assert rhs.getType() instanceof Type;

        NCRV retval = new NCRV((Type)rhs.getType().topUntypedef());

        if (rhs.getEquivType() instanceof FloatType) {

            // get the other value into F0
            int rhs_val_temp = m_symtab.getTemp(4);
            rhs.putValueInto(rhs_val_temp, aw, m_symtab);

            int this_val_temp = m_symtab.getTemp(4);
            retval.setLocation(this_val_temp);

            int rhs_addr_temp = m_symtab.getTemp(4);
            rhs.putAddrInto(rhs_addr_temp, aw, m_symtab);

            // get the value 1 into F1 and convert to float
            int one_temp = m_symtab.getTemp(4);
            aw.write("\t" + "mov" + "\t" + 1 + ", " + Register.G1);
            aw.storeLocal(Register.G1, one_temp);
            aw.loadLocal(one_temp, Register.F1);
            aw.write("\t" + "fitos\t" + Register.F1 + ", " + Register.F1);

            aw.loadLocal(rhs_val_temp, Register.F0);
            switch (op) {
            case PLUSPLUS: 
                aw.write("\t" + "fadds" + "\t" + Register.F0 + ", " + Register.F1 + ", " + Register.F0);
                break;
            case MINUSMINUS:
                aw.write("\t" + "fsubs" + "\t" + Register.F0 + ", " + Register.F1 + ", " + Register.F0);
                break;
            default:
                assert false;
                break;
            }

            aw.storeLocal(Register.F0, this_val_temp);

            // save the incremented value back to the location from which it came
            aw.loadLocal(rhs_addr_temp, Register.G2);
            aw.write("\t" + "st\t" + Register.F0 + ", [" + Register.G2 + "]");

        } else if (rhs.getEquivType() instanceof PointerType) {

            int rhs_val_temp = m_symtab.getTemp(4);
            rhs.putValueInto(rhs_val_temp, aw, m_symtab);

            int this_val_temp = m_symtab.getTemp(4);
            retval.setLocation(this_val_temp);

            int rhs_addr_temp = m_symtab.getTemp(4);
            rhs.putAddrInto(rhs_addr_temp, aw, m_symtab);

            // get size of pointee in L0
            int skipSize = ((Type)((PointerType)rhs.getEquivType()).getPointee()).size();

            aw.write("\t" + "set\t" + skipSize + ", " + Register.L0);
            aw.loadLocal(rhs_val_temp, Register.O0);
            switch (op) {
            case PLUSPLUS: 
                aw.write("\t" + "add" + "\t" + Register.O0 + ", " + Register.L0 + ", " + Register.O0);
                break;
            case MINUSMINUS:
                aw.write("\t" + "sub" + "\t" + Register.O0 + ", " + Register.L0 + ", " + Register.O0);
                break;
            default:
                assert false;
                break;
            }

            // handle return value
            aw.storeLocal(Register.O0, this_val_temp);

            // save the incremented value back to the location from which it came
            aw.loadLocal(rhs_addr_temp, Register.G2);
            aw.write("\t" + "st\t" + Register.O0 + ", [" + Register.G2 + "]");

        } else {
            // not float

            int rhs_val_temp = m_symtab.getTemp(4);
            rhs.putValueInto(rhs_val_temp, aw, m_symtab);

            int this_val_temp = m_symtab.getTemp(4);
            retval.setLocation(this_val_temp);

            int rhs_addr_temp = m_symtab.getTemp(4);
            rhs.putAddrInto(rhs_addr_temp, aw, m_symtab);

            aw.loadLocal(rhs_val_temp, Register.O0);
            switch (op) {
            case PLUSPLUS: 
                aw.write("\t" + "inc" + "\t" + Register.O0);
                break;
            case MINUSMINUS:
                aw.write("\t" + "dec" + "\t" + Register.O0);
                break;
            default:
                assert false;
                break;
            }

            // handle return value
            aw.storeLocal(Register.O0, this_val_temp);

            // save the incremented value back to the location from which it came
            aw.loadLocal(rhs_addr_temp, Register.G2);
            aw.write("\t" + "st\t" + Register.O0 + ", [" + Register.G2 + "]");

        }
        return retval;

    }

    ExprOrReportedErr doPostIncDecOp(Operator op, Expr lhs) {
        aw.writeComment("Entering MyParser.doPostIncDecOp");

        assert op == Operator.PLUSPLUS || op == Operator.MINUSMINUS;

        if (!(lhs.getEquivType() instanceof NumericType) &&
            !( (lhs.getEquivType() instanceof PointerType) && !(  ((PointerType)lhs.getEquivType()).getPointee().untypedef() instanceof FunctionType))  ) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error2_Type, lhs.getType().getName(), op.toString()));
            return new ReportedErr();
        }

        if (!(lhs instanceof MLV)) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error2_Lval, op.toString()));
            return new ReportedErr();
        }

        assert lhs.getType() instanceof Type;

        NCRV retval = new NCRV((Type)lhs.getType().topUntypedef());

        if (lhs.getEquivType() instanceof FloatType) {

            int lhs_addr_temp = m_symtab.getTemp(4);
            lhs.putAddrInto(lhs_addr_temp, aw, m_symtab);

            int lhs_val_temp = m_symtab.getTemp(4);
            lhs.putValueInto(lhs_val_temp, aw, m_symtab);

            int this_val_temp = m_symtab.getTemp(4);
            retval.setLocation(this_val_temp);

            int one_temp = m_symtab.getTemp(4);
            aw.write("\t" + "mov" + "\t" + 1 + ", " + Register.G1);
            aw.storeLocal(Register.G1, one_temp);

            aw.loadLocal(lhs_val_temp, Register.F0);
            aw.storeLocal(Register.F0, this_val_temp);
            aw.loadLocal(one_temp, Register.F1);
            aw.write("\t" + "fitos\t" + Register.F1 + ", " + Register.F1);
            switch (op) {
            case PLUSPLUS: 
                aw.write("\t" + "fadds" + "\t" + Register.F0 + ", " + Register.F1 + ", " + Register.F0);
                break;
            case MINUSMINUS:
                aw.write("\t" + "fsubs" + "\t" + Register.F0 + ", " + Register.F1 + ", " + Register.F0);
                break;
            default:
                assert false;
                break;
            }

            aw.loadLocal(lhs_addr_temp, Register.G2);
            aw.write("\t" + "st\t" + Register.F0 + ", [" + Register.G2 + "]");

        } else if (lhs.getEquivType() instanceof PointerType) {

            aw.writeComment("Entering the MyParser.doPostIncDecOp PointerType section");

            aw.writeComment("Putting lhs value into a temp");
            int lhs_val_temp = m_symtab.getTemp(4);
            lhs.putValueInto(lhs_val_temp, aw, m_symtab);

            // handle return value
            int this_val_temp = m_symtab.getTemp(4);
            retval.setLocation(this_val_temp);

            aw.writeComment("Putting lhs address into a temp");
            int lhs_addr_temp = m_symtab.getTemp(4);
            lhs.putAddrInto(lhs_addr_temp, aw, m_symtab);

            aw.writeComment("Storing the return value from this postIncDecOp into " + this_val_temp);
            aw.loadLocal(lhs_val_temp, Register.O0);
            aw.storeLocal(Register.O0, this_val_temp);

            // get size of pointee in L0
            aw.writeComment("Setting %l0 to the size of the pointee");
            int skipSize = ((Type)((PointerType)lhs.getEquivType()).getPointee()).size();
            aw.write("\t" + "set\t" + skipSize + ", " + Register.L0);

            switch (op) {
            case PLUSPLUS: 
                aw.write("\t" + "add" + "\t" + Register.O0 + ", " + Register.L0 + ", " + Register.O0);
                break;
            case MINUSMINUS:
                aw.write("\t" + "sub" + "\t" + Register.O0 + ", " + Register.L0 + ", " + Register.O0);
                break;
            default:
                assert false;
                break;
            }

            aw.writeComment("Loading lhs_addr into %g2");
            aw.loadLocal(lhs_addr_temp, Register.G2);
            aw.writeComment("and updating the memory belonging to lhs");
            aw.write("\t" + "st\t" + Register.O0 + ", [" + Register.G2 + "]");

            aw.writeComment("Leaving the MyParser.doPostIncDecOp PointerType section");

        } else {
            int lhs_val_temp = m_symtab.getTemp(4);
            lhs.putValueInto(lhs_val_temp, aw, m_symtab);

            int this_val_temp = m_symtab.getTemp(4);
            retval.setLocation(this_val_temp);

            int lhs_addr_temp = m_symtab.getTemp(4);
            lhs.putAddrInto(lhs_addr_temp, aw, m_symtab);

            aw.loadLocal(lhs_val_temp, Register.O0);        
            aw.storeLocal(Register.O0, this_val_temp);

            switch (op) {
            case PLUSPLUS: 
                aw.write("\t" + "inc" + "\t" + Register.O0);
                break;
            case MINUSMINUS:
                aw.write("\t" + "dec" + "\t" + Register.O0);
                break;
            default:
                assert false;
                break;
            }

            aw.loadLocal(lhs_addr_temp, Register.G2);
            aw.write("\t" + "st\t" + Register.O0 + ", [" + Register.G2 + "]");

        }


        aw.writeComment("Leaving MyParser.doPostIncDecOp");

        return retval;

    }


    void checkConditional(Expr e) {
        Type eType = e.getType();

        if (!eType.isTypedefEquivalent(new BooleanType()) &&
            !eType.isTypedefEquivalent(new IntType()))
        {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error4_Test, eType.getName()));
        }
                 
    }

    void doIfAfterCondition(Expr e) { 

        aw.write("\t!\t" + "in if statement, about to see if the conditional was true or not");

        String elseLabel = LabelMaker.getLabel();
        elseLabels.push(elseLabel);

        int cond_val_temp = m_symtab.getTemp(4);
        e.putValueInto(cond_val_temp, aw, m_symtab);
        aw.loadLocal(cond_val_temp, Register.O0);

        aw.write("\t" + "cmp" + "\t" + Register.O0 + ", " + Register.G0);
        aw.write("\t" + "be" + "\t" + elseLabel);
        aw.write("\t" + "nop");
        
    }

    void doIfAfterThen() { 
        String elseLabel = elseLabels.pop();
        String afterElseLabel = LabelMaker.getLabel();
        afterElseLabels.push(afterElseLabel);

        aw.write("\t!\t" + "in if statement, right after the then clause");

        aw.write("\t" + "ba" + "\t" + afterElseLabel + "\t!\t" + "afterElseLabel");
        aw.write("\t" + "nop");

        aw.write("");
        aw.write(elseLabel + ":" + "\t!\t" + "elseLabel");

    }

    void doIfAfterElse() { 
        String afterElseLabel = afterElseLabels.pop();

        aw.write(afterElseLabel + ":" + "\t!\t" + "afterElseLabel");
        aw.write("");
    }
    

    void checkPlainReturn() {
        Scope currentScope = m_symtab.getCurrentScope();
        if (currentScope instanceof FunctionScope) {
            ((FunctionScope)currentScope).setSeenReturn();
        }

        Expr func = (Expr)m_symtab.getFunc();
        assert func.getType() instanceof FunctionType;
        FunctionType ftype = (FunctionType)func.getType();
        
        if (!(ftype.getReturnType() instanceof VoidType)) {
            m_nNumErrors++;
            m_errors.print(ErrorMsg.error6a_Return_expr);
        }

        aw.write("\tret");
        aw.write("\trestore");

    }

    void checkReturn(Expr e) {
        Scope currentScope = m_symtab.getCurrentScope();
        if (currentScope instanceof FunctionScope) {
            ((FunctionScope)currentScope).setSeenReturn();
        }

        Expr func = (Expr)m_symtab.getFunc();
        assert func.getType() instanceof FunctionType;
        FunctionType ftype = (FunctionType)func.getType();

        if (!(ftype.getReturnType().isAssignableFrom(e.getType()))) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error6a_Return_type,
                                              e.getType().getName(),
                                              ftype.getReturnType().getName()));
        }

        int retval_temp = m_symtab.getTemp(4);
        e.putValueInto(retval_temp, aw, m_symtab);

        aw.write("\t!\t" + "returning...");

        if (e.getType().untypedef() instanceof IntType &&
            ftype.getReturnType().untypedef() instanceof FloatType)
        {
            aw.loadLocal(retval_temp, Register.F0);
            aw.write("\t" + "fitos\t" + Register.F0 + ", " + Register.F0);
            aw.storeLocal(Register.F0, retval_temp);
        }

        aw.loadLocal(retval_temp, Register.I0);

        aw.write("\tret");
        aw.write("\trestore");
        aw.write("");

    }

    void doBadReturn() {
        Scope currentScope = m_symtab.getCurrentScope();
        if (currentScope instanceof FunctionScope) {
            ((FunctionScope)currentScope).setSeenReturn();
        }
    }


    void checkExit(Expr e) {
        if (!((new IntType()).isAssignableFrom(e.getType()))) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error7_Exit,
                                              e.getType().getName()));
        }

        int val_temp = m_symtab.getTemp(4);
        e.putValueInto(val_temp, aw, m_symtab);

        aw.loadLocal(val_temp, Register.O0);
        aw.write("\t" + "call\t" + "exit");
        aw.write("\t" + "nop");

    }


    /// \todo actually only returns intvalue or reported err.  need to
    /// make yet another type.
    IntValueOrNothingOrReportedErr checkArrayDef(ConstExprOrConstErrOrReportedErr maybeCe) {
        if (maybeCe instanceof ConstExpr) {
            ConstExpr cexp = (ConstExpr)maybeCe;

            if (cexp.getValue() instanceof IntValue) {
                IntValue val = (IntValue)cexp.getValue();

                if (val.getValue() <= 0) {
                    m_nNumErrors++;
                    m_errors.print(Formatter.toString(ErrorMsg.error10z_Array,
                                                      val.getValue()));
                    return new ReportedErr();
                }

                return val;
            } else {
                m_nNumErrors++;
                m_errors.print(Formatter.toString(ErrorMsg.error10i_Array,
                                                  cexp.getType().getName()));
                return new ReportedErr();
            }
                
        } else if (maybeCe instanceof ConstErr) {
            ConstErr cerr = (ConstErr)maybeCe;

            if (!(cerr.type.isTypedefEquivalent(new IntType()))) {
                m_nNumErrors++;
                m_errors.print(Formatter.toString(ErrorMsg.error10i_Array,
                                                  cerr.type.getName()));
                return new ReportedErr();
            } else {
                m_nNumErrors++;
                m_errors.print(ErrorMsg.error10c_Array);
                return new ReportedErr();
            }
        } else if (maybeCe instanceof ReportedErr) {
            return (ReportedErr)maybeCe;
        } else {
            assert false;
            return null;
        }

    }

    void beginForBlock() {
        m_symtab.openForScope();
    }

    void endForBlock() {
        m_symtab.closeScope();
    }

    void checkBreak() {
        if (!m_symtab.inForScope()) {
            m_nNumErrors++;
            m_errors.print(ErrorMsg.error12_Break);
        }
        
        aw.write("\t" + "ba" + "\t" + forLabelBases.peek() + "_done");
        aw.write("\t" + "nop");
        
    }

    void checkContinue() {
        if (!m_symtab.inForScope()) {
            m_nNumErrors++;
            m_errors.print(ErrorMsg.error12_Continue);
        }

        aw.write("\t" + "ba" + "\t" + forLabelBases.peek() + "_update");
        aw.write("\t" + "nop");

    }

    ExprOrReportedErr getThis() {
        IncompleteStructType ist = m_symtab.getThisType();
        if (ist != null) {
            return new ThisExpr(ist);
        } else {
            m_nNumErrors++;
            m_errors.print("extra error: this used outside struct");
            return new ReportedErr();
        }
    }

    ExprOrReportedErr dereference(Expr e) {

        aw.writeComment("Entering MyParser.dereference");
        aw.writeComment("e is " + e.toString());

        Type t = e.getType().topUntypedef();
        if (!(t instanceof PointerType)) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error15_Receiver, e.getType().getName()));
            return new ReportedErr();
        }

        PointerType pt = (PointerType)t;
        Type pointee = pt.getPointee();

        emitPointerCheck(e, Register.L0, Register.L1);

        if (pointee.untypedef() instanceof FunctionType) {
            aw.writeComment("Leaving MyParser.dereference, returning an FuncPtrFromDeref, operand is e");
            FuncPtrFromDeref retval = new FuncPtrFromDeref((FunctionType)pointee.untypedef(), e);
            return retval;
        } else {
            aw.writeComment("Leaving MyParser.dereference, returning an NCDerefRes, operand is e");
            NCDerefRes retval = new NCDerefRes(pointee);
            retval.setOperand(e);
            return retval;
        }

    }

    ExprOrReportedErr doArrow(Expr e, String s) {
        Type t = e.getType().topUntypedef();
        if (!((t instanceof PointerType) &&
              ((((PointerType)t).getPointee().topUntypedef() instanceof StructType) ||
               (((PointerType)t).getPointee().topUntypedef() instanceof IncompleteStructType))))
        {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error15_ReceiverArrow, e.getType().getName()));
            return new ReportedErr();
        }

        Type pointee = ((PointerType)t).getPointee();

        NCDerefRes dereffedExpr = new NCDerefRes((Type)pointee);
        dereffedExpr.setOperand(e);

        emitPointerCheck(e, Register.L0, Register.L1);

        if (pointee instanceof StructType)
            return lookInStruct(dereffedExpr, (StructType)pointee, s);
        else if (pointee instanceof IncompleteStructType)
            return lookInStruct(dereffedExpr, (IncompleteStructType)pointee, s);
        else {
            assert false;
            return null;
        }
        
    }

    ExprOrReportedErr lookInStruct(Expr theStruct, StructType st, String id) {

        StructScope members = st.getMembers();

        STO obj = members.accessMember(id);

        if (obj == null) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error14f_StructExp, id, st.getName()));
            return new ReportedErr();
        } else if (obj instanceof ErrorSTO) {
            // do nothing.  we've already reported this.
        } else if (obj instanceof Type) {
            // you're not supposed to be able to put these in structdefs
            assert false;
        } else if (obj instanceof UnboundFunction) {
            UnboundFunction unbFunc = (UnboundFunction)obj;
            return new BoundFunction(unbFunc.getFullName(), unbFunc.getType(), theStruct);
        } else if (obj instanceof Expr && ((Expr)obj).getType().untypedef() instanceof FunctionType) {
            int offsetInStruct = members.getOffset(id);
            return new FuncPtrFromStruct( (FunctionType) ((Expr)obj).getType().untypedef(),
                                         theStruct, offsetInStruct);
        } else if (obj instanceof NCVar) {
            int offsetInStruct = members.getOffset(id);
            return new StructAccess(theStruct, ((NCVar)obj).getType(), offsetInStruct);
        } else if (obj instanceof ArrayName) {
            int offsetInStruct = members.getOffset(id);
            return (ArrayName)obj;
        } 

        assert false;
        return null;

    }

    ExprOrReportedErr lookInStruct(Expr theStruct, IncompleteStructType st, String id) {
        StructScope members = st.getMembers();

        STO obj = members.accessMember(id);

        if (obj == null) {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error14b_StructExpThis, id));
            return new ReportedErr();
        } else if (obj instanceof ErrorSTO) {
            // do nothing.  we've already reported this.
        } else if (obj instanceof Type) {
            // you're not supposed to be able to put these in structdefs
            assert false;
        } else if (obj instanceof UnboundFunction) {
            UnboundFunction unbFunc = (UnboundFunction)obj;
            return new BoundFunction(unbFunc.getFullName(), unbFunc.getType(), theStruct);
        } else if (obj instanceof Expr && ((Expr)obj).getType().untypedef() instanceof FunctionType) {
            int offsetInStruct = members.getOffset(id);
            return new FuncPtrFromStruct(  (FunctionType) ((Expr)obj).getType().untypedef(),
                                           theStruct, offsetInStruct);
        } else if (obj instanceof NCVar) {
            int offsetInStruct = members.getOffset(id);
            return new StructAccess(theStruct, ((NCVar)obj).getType(), offsetInStruct);
        } else if (obj instanceof ArrayName) {
            int offsetInStruct = members.getOffset(id);
            return (ArrayName)obj;
        }

        assert false;
        return null;

    }

    ExprOrReportedErr getAddress(Expr e) {
        if (e instanceof NMLV || e instanceof MLV)
        {
            NCRV retval = new NCRV(new PointerType(e.getType()));

            int addr_temp = m_symtab.getTemp(4);
            e.putAddrInto(addr_temp, aw, m_symtab);
            retval.setLocation(addr_temp);

            return retval;

        } else {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error21_AddressOf, e.getType().getName()));
            return new ReportedErr();
        }
    }

    void doNew(Expr e) {
        if (!(e instanceof MLV)) {
            m_nNumErrors++;
            m_errors.print(ErrorMsg.error16_New_var);
            return;
        }


        if (!((e.getType().topUntypedef() instanceof PointerType) &&
              !(((PointerType)(e.getType().topUntypedef())).getPointee().untypedef() instanceof FunctionType))) 
        {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error16_New, e.getType().getName()));
            return;
        }

        int addr_temp = m_symtab.getTemp(4);
        e.putAddrInto(addr_temp, aw, m_symtab);

        String sizeString;
        if ((Type)((PointerType)e.getEquivType()).getPointee() instanceof IncompleteStructType) {


            sizeString  = sizeName(((IncompleteStructType)(Type)((PointerType)e.getEquivType()).getPointee()).getName());


        } else if ((Type)((PointerType)e.getEquivType()).getPointee() instanceof StructType) {
            sizeString  = sizeName(((StructType)(Type)((PointerType)e.getEquivType()).getPointee()).getName());
        } else {
            int theSize = ((Type)((PointerType)e.getEquivType()).getPointee()).size();
            sizeString = "" + theSize;
        }



        aw.write("\t" + "set\t" + 1 + ", " + Register.O0);
        aw.write("\t" + "set\t" + sizeString + ", " + Register.O1);
        aw.write("\t" + "call\t" + "calloc");
        aw.write("\t" + "nop");
        
        // load &e into L0
        aw.loadLocal(addr_temp, Register.L0);
        aw.write("\t" + "st\t" + Register.O0 + ", [" + Register.L0 + "]");

        
    }

    void doDelete(Expr e) {
        if (!(e instanceof MLV)) {
            m_nNumErrors++;
            m_errors.print(ErrorMsg.error16_Delete_var);
            return;
        }

        if (!(e.getType().topUntypedef() instanceof PointerType))
        {
            m_nNumErrors++;
            m_errors.print(Formatter.toString(ErrorMsg.error16_Delete, e.getType().getName()));
            return;
        }
        
        emitPointerCheck(e, Register.L0, Register.L1);

        int addr_temp = m_symtab.getTemp(4);
        e.putAddrInto(addr_temp, aw, m_symtab);

        // put &e into L0
        aw.loadLocal(addr_temp, Register.L0);

        // put e into O0
        aw.write("\t" + "ld\t" + "[" + Register.L0 + "], " + Register.O0);

        // zero out the pointer
        aw.write("\t" + "st\t" + Register.G0 + ", [" + Register.L0 + "]");

    }

    ExprOrReportedErr doCast(Type type, Expr e) {
        Type targetType = type.topUntypedef();
        Type eType = e.getType().untypedef();

        if ((e instanceof ConstExpr) &&
            (eType instanceof IntType || eType instanceof FloatType || eType instanceof BooleanType || eType instanceof PointerType || eType instanceof NullType))
        {
            if (targetType instanceof IntType) return new CRV(((ObjectValue)((ConstExpr)e).getValue()).makeIntValue());
            if (targetType instanceof FloatType) return new CRV(((ObjectValue)((ConstExpr)e).getValue()).makeFloatValue());
            if (targetType instanceof BooleanType) return new CRV(((ObjectValue)((ConstExpr)e).getValue()).makeBooleanValue());
            if (targetType instanceof PointerType) {
                int val = ((ObjectValue)((ConstExpr)e).getValue()).makeIntValue().getValue();
                return new CRV(new PointerValue((PointerType)targetType, val));
            }
        }

        if ((eType instanceof IntType || eType instanceof FloatType || eType instanceof BooleanType || eType instanceof PointerType) &&
            (targetType instanceof IntType || targetType instanceof FloatType || targetType instanceof BooleanType || targetType instanceof PointerType))
        {
            NCRV retval = new NCRV(type);

            if (eType instanceof FloatType && !(targetType instanceof FloatType))
            {
                // from float to non-float
                int from_value_temp = m_symtab.getTemp(4);
                e.putValueInto(from_value_temp, aw, m_symtab);

                int ret_value_temp =  m_symtab.getTemp(4);

                aw.loadLocal(from_value_temp, Register.F0);
                aw.write("\t" + "fstoi\t" + Register.F0 + " ," + Register.F0);
                aw.storeLocal(Register.F0, ret_value_temp);

                retval.setLocation(ret_value_temp);
            } 
            else if (!(eType instanceof FloatType) && targetType instanceof FloatType)
            {
                // from non-float to float
                int from_value_temp = m_symtab.getTemp(4);
                e.putValueInto(from_value_temp, aw, m_symtab);

                int ret_value_temp =  m_symtab.getTemp(4);

                aw.loadLocal(from_value_temp, Register.F0);
                aw.write("\t" + "fitos\t" + Register.F0 + " ," + Register.F0);
                aw.storeLocal(Register.F0, ret_value_temp);

                retval.setLocation(ret_value_temp);
            }
            else if (type instanceof BooleanType)
            {
                String storeLabel = LabelMaker.getLabel();

                int from_value_temp = m_symtab.getTemp(4);
                e.putValueInto(from_value_temp, aw, m_symtab);

                int ret_value_temp =  m_symtab.getTemp(4);
                retval.setLocation(ret_value_temp);

                aw.loadLocal(from_value_temp, Register.L0);
                aw.write("\t" + "cmp\t" + Register.L0 + ", " + Register.G0);
                aw.write("\t" + "be\t" + storeLabel);
                aw.write("\t" + "nop");

                // so it was true
                aw.write("\t" + "set\t" + 1 +"," +  Register.L0);

                aw.write(storeLabel + ":");
                aw.storeLocal(Register.L0, ret_value_temp);
            }
            else 
            {
                // just copy the bits

                int from_value_temp = m_symtab.getTemp(4);
                e.putValueInto(from_value_temp, aw, m_symtab);

                int ret_value_temp =  m_symtab.getTemp(4);

                aw.loadLocal(from_value_temp, Register.L0);
                aw.storeLocal(Register.L0, ret_value_temp);

                retval.setLocation(ret_value_temp);
                
            }

            return retval;
        }
        
        m_nNumErrors++;
        m_errors.print(Formatter.toString(ErrorMsg.error20_Cast, e.getType().getName(), type.getName()));
        return new ReportedErr();
    }

    ExprOrReportedErr doSizeof(ExprOrReportedErr maybeExpr) {
        if (maybeExpr instanceof ReportedErr) {
            return (ReportedErr)maybeExpr;
        } else if (maybeExpr instanceof Expr) {
            if (maybeExpr instanceof NMLV) {
                NMLV expr = (NMLV) maybeExpr;
                Type type = expr.getType();
                if (type instanceof Type) {
                    return new CRV(new IntValue(((Type)type).size()));
                } else {
                    // then it was probably a function name
                    m_nNumErrors++;
                    m_errors.print("operand of sizeof does not have object type.");
                    return new ReportedErr();
                }
            } else if (maybeExpr instanceof MLV) {
                MLV expr = (MLV) maybeExpr;
                Type type = expr.getType();
                if (type instanceof Type) {
                    return new CRV(new IntValue(((Type)type).size()));
                } else {
                    // something weird happened
                    m_nNumErrors++;
                    m_errors.print("operand of sizeof does not have object type.");
                    return new ReportedErr();
                }
            } else if (maybeExpr instanceof RV) { 
                m_nNumErrors++;
                m_errors.print(ErrorMsg.error19_Sizeof);
                return new ReportedErr();
            } else if (maybeExpr instanceof VoidExpr) {
                // you'd have to enter a void variable into the symbol
                // table for this to happen.  that should be
                // impossible.
                assert false;
                return null;
            } else {
                // i'd have to have forgotten some subtype of Expr for
                // us to get here.
                assert false;
                return null;
            }
        } else {
            assert false;
            return null;
        }
    }

    ExprOrReportedErr doSizeof(TypeOrReportedErr t) {
        if (t instanceof Type) {
            Type ot = (Type)t;
            return new CRV(new IntValue(ot.size()));
        } else if (t instanceof ReportedErr) {
            return (ReportedErr) t;
        } else {
            assert false;
            return null;
        }
    }

    ExprOrReportedErr doUnarySign(Operator op, ExprOrReportedErr maybeExpr) {
        if (maybeExpr instanceof ReportedErr) {
            return maybeExpr;
        } else if (maybeExpr instanceof Expr) {
            Expr expr = (Expr) maybeExpr;
            if (expr.getType().untypedef() instanceof IntType) {
                if (op == Operator.MINUS) {
                    if (expr instanceof ConstExpr) {
                        ConstExpr retval = ((ConstExpr)expr).getNegated();
                        return retval;
                    } else {
                        NCRV retval = new NCRV(new IntType());

                        aw.write("\t!\t" + "negating an int");
                        int rhs_val_temp = m_symtab.getTemp(4);
                        expr.putValueInto(rhs_val_temp, aw, m_symtab);
                        aw.loadLocal(rhs_val_temp, Register.O0);
                        aw.write("\t" + "sub" + "\t" + Register.G0 + ", " + Register.O0 + ", " + Register.O0);

                        int result_val_temp  = m_symtab.getTemp(4);
                        retval.setLocation(result_val_temp);
                        aw.storeLocal(Register.O0, result_val_temp);

                        return retval;
                    }
                } else if (op == Operator.PLUS) {
                    if (expr instanceof ConstExpr) {
                        return new CRV((ObjectValue)((ConstExpr)expr).getValue());
                    } else {
                        NCRV retval = new NCRV(new IntType());

                        aw.write("\t!\t" + "negating an int");
                        int rhs_val_temp = m_symtab.getTemp(4);
                        expr.putValueInto(rhs_val_temp, aw, m_symtab);
                        aw.loadLocal(rhs_val_temp, Register.O0);

                        int result_val_temp  = m_symtab.getTemp(4);
                        retval.setLocation(result_val_temp);
                        aw.storeLocal(Register.O0, result_val_temp);

                        return retval;
                    }

                } else {
                    assert false;
                    return null;
                }
            } else  if (expr.getType().untypedef() instanceof FloatType) {
                if (op == Operator.MINUS) {
                    if (expr instanceof ConstExpr) {
                        return ((ConstExpr)expr).getNegated();
                    } else {
                        NCRV retval = new NCRV(new FloatType());

                        int rhs_val_temp = m_symtab.getTemp(4);
                        expr.putValueInto(rhs_val_temp, aw, m_symtab);
                        int zero_val_temp = m_symtab.getTemp(4);

                        aw.storeLocal(Register.G0, zero_val_temp);
                        aw.loadLocal(zero_val_temp, Register.F0);
                        aw.loadLocal(rhs_val_temp, Register.F1);
                        aw.write("\t" + "fsubs\t" + Register.F0 + ", " + Register.F1 + ", " + Register.F1);

                        int result_val_temp = m_symtab.getTemp(4);
                        retval.setLocation(result_val_temp);
                        aw.storeLocal(Register.F1, result_val_temp);

                        return retval;
                    }
                } else if (op == Operator.PLUS) {
                    if (expr instanceof ConstExpr) {
                        return new CRV((ObjectValue)((ConstExpr)expr).getValue());
                    } else {
                        NCRV retval = new NCRV(new FloatType());

                        int rhs_val_temp = m_symtab.getTemp(4);
                        expr.putValueInto(rhs_val_temp, aw, m_symtab);

                        aw.loadLocal(rhs_val_temp, Register.F1);

                        int result_val_temp = m_symtab.getTemp(4);
                        retval.setLocation(result_val_temp);
                        aw.storeLocal(Register.F1, result_val_temp);

                        return retval;
                    }
                } else {
                    assert false;
                    return null;
                }
            } else {
                // spec says we don't need to check for this
                m_nNumErrors++;
                m_errors.print("Trying to negate non-numeric value.");
                return new ReportedErr();
            }
        } else {
            assert false;
            return null;
        }
    }

    void write(ExprOrReportedErr thing) {
        if (thing instanceof ReportedErr) return;

        if (!(thing instanceof Expr)) assert false;

        Expr e = (Expr) thing;

        if (e.getType().untypedef() instanceof IntType) {
            int value_temp = m_symtab.getTemp(4);
            e.putValueInto(value_temp, aw, m_symtab);

            // load value into O1
            aw.loadLocal(value_temp, Register.O1);
            // load int format into O0
            aw.write("\t" + "set\t__int_format, " + Register.O0);
            // make func call
            aw.write("\tcall\tprintf");
            aw.write("\tnop");
            aw.write("");

        } else if (e.getType().untypedef() instanceof FloatType) {
            int value_temp = m_symtab.getTemp(4);
            e.putValueInto(value_temp, aw, m_symtab);

            aw.loadLocal(value_temp, Register.F0);
            aw.write("\tcall\tprintFloat");
            aw.write("\tnop");
        } else if (e.getType().untypedef() instanceof BooleanType) {
            int value_temp = m_symtab.getTemp(4);
            e.putValueInto(value_temp, aw, m_symtab);
                
            // load value into %g1
            aw.loadLocal(value_temp, Register.G1);

            String false_label = LabelMaker.getLabel();
            String done_label = LabelMaker.getLabel();

            // do comparison
            aw.write("\tcmp\t" + Register.G0 + ", " + Register.G1);
            // go to false?
            aw.write("\tbe\t" + false_label);
            aw.write("\tnop");
            // set %o1 to the true string
            aw.write("\tset\t__true_string, " + Register.O1);
            // go to done
            aw.write("\tba\t" + done_label);
            aw.write("\tnop");
            // the false label
            aw.write(false_label + ":");
            // set %o1 to the false string
            aw.write("\tset\t" + "__false_string, " + Register.O1);
            // the done label
            aw.write(done_label + ":");
            aw.write("\t" + "set" + "\t" + "__str_format" + ", " + Register.O0);
            aw.write("\t" + "call" + "\t" + "printf");
            aw.write("\tnop");
                    
        } else if (e.getType().untypedef() instanceof PointerType &&
                   ((PointerType)e.getType().untypedef()).getPointee() instanceof CharType)
        {
            int value_temp = m_symtab.getTemp(4);
            e.putValueInto(value_temp, aw, m_symtab);

            aw.loadLocal(value_temp, Register.O1);
            aw.write("\t" + "set" + "\t" + "__str_format" + ", " + Register.O0);
            aw.write("\t" + "call" + "\t" + "printf");
            aw.write("\t" + "nop");
        } else {
            assert false;
        }

    }

    void read(Expr maybe_mlv) {
        if (!(maybe_mlv instanceof MLV)) {
            m_nNumErrors++;
            m_errors.print("Trying to read into a expression that is not a modifiable lval.");
        }

        MLV expr = (MLV)maybe_mlv;

        int into_addr_temp = m_symtab.getTemp(4);
        expr.putAddrInto(into_addr_temp, aw, m_symtab);
        aw.loadLocal(into_addr_temp, Register.G2);

        if (expr.getType().untypedef() instanceof IntType) {
            aw.write("\t" + "call\t" + "inputInt");
            aw.write("\t" + "nop");
            aw.write("\t" + "st\t" + Register.O0 + ", [" + Register.G2 + "]");
        } else if (expr.getType().untypedef() instanceof FloatType) {
            aw.write("\t" + "call\t" + "inputFloat");
            aw.write("\t" + "nop");
            aw.write("\t" + "st\t" + Register.F0 + ", [" + Register.G2 + "]");            
        } else {
            m_nNumErrors++;
            m_errors.print("Trying to using cin to read into a non-numeric variable.");
        }
        
    }

    void doFor1() {
        forLabelBases.push(LabelMaker.getLabel());
        aw.write(forLabelBases.peek() + "_test:");
    }

    // e could be null, which stands for true
    void doFor2(ExprOrReportedErr maybeExpr) {


        if (maybeExpr != null) {

            if (maybeExpr instanceof ReportedErr) return;

            Expr e = (Expr) maybeExpr;

            int cond_val_temp = m_symtab.getTemp(4);
            e.putValueInto(cond_val_temp, aw, m_symtab);

            aw.loadLocal(cond_val_temp, Register.G1);
            aw.write("\t" + "cmp" + "\t" + Register.G0 + ", " + Register.G1);
            aw.write("\t" + "be" + "\t" + forLabelBases.peek() + "_done");
            aw.write("\t" + "nop");
            aw.write("\t" + "ba" + "\t" + forLabelBases.peek() + "_body");
            aw.write("\t" + "nop");
        } else {

            aw.write("\t" + "ba" + "\t" + forLabelBases.peek() + "_body");
            aw.write("\t" + "nop");

        }

        aw.write("");
        aw.write(forLabelBases.peek() + "_update:");
    }

    void doFor3() {
        aw.write("\t" + "ba" + "\t" + forLabelBases.peek() + "_test");
        aw.write("\t" + "nop");
        aw.write("");
        aw.write(forLabelBases.peek() + "_body:");
    }

    void doFor4() {
        aw.write("\t" + "ba" + "\t" + forLabelBases.peek() + "_update");
        aw.write("\t" + "nop");
        aw.write(forLabelBases.peek() + "_done:");

        forLabelBases.pop();
    }

    void doStatic(boolean s) {
        isStatic = s;
        
        // if we're dealing with a static local declaration
        if (s && m_symtab.getFunc() != null) {
            endLocalStaticInitLabel = LabelMaker.getLabel();
            
            String alreadyInitMemLabel = endLocalStaticInitLabel + "_already_init";

            // create a spot in the .bss marking whether we've already been here
            aw.write("\t" + ".section\t" + "\".bss\"");
            aw.write(alreadyInitMemLabel + ":");
            aw.write("\t" + ".skip\t" + 4);

            aw.write("\t" + ".section\t" + "\".text\"");

            // if we *have* already been here, skip ahead
            aw.write("\t" + "set\t" + alreadyInitMemLabel + ", " + Register.L0);
            aw.write("\t" + "ld\t" + "[" + Register.L0 + "], " +
                     Register.L1);
            aw.write("\t" + "cmp\t" + Register.L1 +", " + Register.G0);
            aw.write("\t" + "bne\t" + endLocalStaticInitLabel);
            aw.write("\t" + "nop");
            

            // since we're here, that means we're running the
            // initialization code, so 
            aw.write("\t" + "set\t" + 1 + ", " + Register.L1);
            aw.write("\t" + "st\t" + Register.L1 + ", [" + Register.L0 + "]");

            
        }

        

    }

    void doExternFunc(TypeOrReportedErr returnType, String id, Vector<ParamDeclOrReportedErr> params)
    {
        
        boolean looksGood = true;

        if (m_symtab.checkStructMember(id) == true ||
            m_symtab.accessLocal(id) != null)
        {
            looksGood = false;
            m_nNumErrors++;
            if (m_symtab.inStructScope()) {
                m_errors.print(Formatter.toString(ErrorMsg.error13a_Struct, id));
            } else {
                m_errors.print (Formatter.toString(ErrorMsg.redeclared_id, id));
            }
        }

        if (returnType instanceof ReportedErr) {
            looksGood = false;
        }

        for (ParamDeclOrReportedErr pdore : params) {
            if (pdore instanceof ReportedErr) {
                looksGood = false;
            }
        }


        if (looksGood) {
            Vector<ParamDecl> goodParams = new Vector<ParamDecl>();
            for (ParamDeclOrReportedErr pd : params)
                goodParams.add((ParamDecl)pd);

            FunctionType type = new FunctionType((Type)returnType, goodParams);
            m_symtab.insert(new NamedFunction(id, type));
            
        } else {
            m_symtab.insert(new ErrorSTO(id));
        }

    }


    void emitPointerCheck(Expr ptr, Register tempRegister, Register tempRegister2) {
        
            String notNullLabel = LabelMaker.getLabel();

            int ptrValueTemp = m_symtab.getTemp(4);
            ptr.putValueInto(ptrValueTemp, aw, m_symtab);

            aw.loadLocal(ptrValueTemp, tempRegister);
            aw.write("\t" + "cmp\t" + tempRegister + ", " + Register.G0);
            aw.write("\t" + "bne\t" + notNullLabel);
            aw.write("\t" + "nop");

            aw.write("\t" + "set\t" + "__null_pointer_dereference, " + Register.O0);
            aw.write("\t" + "call\t" + "printf");
            aw.write("\t" + "nop");
            aw.write("\t" + "mov\t" + 1 + ", " + Register.O0);
            aw.write("\t" + "call\t" + "exit");
            aw.write("\t" + "nop");

            aw.write(notNullLabel + ":");

            String noProblemsLabel = LabelMaker.getLabel();

            //
            // do deallocated stack pointer check
            //

            // get value at .lowest_sp_yet intto tempRegister2
            aw.write("\t" + "set\t" + ".lowest_sp_yet, " + tempRegister2);
            aw.write("\t" + "ld\t" + "[" + tempRegister2 + "], " + tempRegister2);
            // you're good if  ptr <= that value
            aw.write("\t" + "cmp\t" + tempRegister + ", " + tempRegister2);
            aw.write("\t" + "bleu\t" + noProblemsLabel);
            aw.write("\t" + "nop");

            // get %sp + 92 into tempRegister2
            aw.write("\t" + "add\t" + "%sp, " + 92 + ", " + tempRegister2);
            // you're good if ptr >= that value
            aw.write("\t" + "cmp\t" + tempRegister + ", " + tempRegister2);
            aw.write("\t" + "bgeu\t" + noProblemsLabel);
            aw.write("\t" + "nop");

            // still here, so you've got a problem
            aw.write("\t" + "set\t" + "__deallocated_stack_pointer_dereference, " + Register.O0);
            aw.write("\t" + "call\t" + "printf");
            aw.write("\t" + "nop");
            aw.write("\t" + "mov\t" + 1 + ", " + Register.O0);
            aw.write("\t" + "call\t" + "exit");
            aw.write("\t" + "nop");

            // done checking
            aw.write(noProblemsLabel + ":\t! noProblemsLabel");


    }

    Expr emitFunctionPointerNullComparison(Operator op, AbstractFunctionPointer fp) {
        NCRV retval = new NCRV(new BooleanType());

        int pointeeTemp = m_symtab.getTemp(4);
        fp.putAddrInto(pointeeTemp, aw, m_symtab);
        aw.loadLocal(pointeeTemp, Register.L0);
        aw.write("\t" + "ld\t" + "[" + Register.L0 + "], " + Register.L0);
        aw.write("\t" + "cmp\t" + Register.L0 + ", " + Register.G0);

        String branchCode;
        switch (op) {
        case EQU: branchCode = "be"; break;
        case NEQ: branchCode = "bne"; break;
        default: branchCode = ""; assert false;
        }

        String trueLabel = LabelMaker.getLabel();
        String doneLabel = LabelMaker.getLabel();
            
        aw.write("\t" + branchCode + "\t" + trueLabel);
        aw.write("\tnop");
        aw.write("\tmov\t0, " + Register.O0);
        aw.write("\tba\t" + doneLabel);
        aw.write("\tnop");

        aw.write(trueLabel + ":");
        aw.write("\tmov\t1, " + Register.O0);
        aw.write(doneLabel + ":");

        // the value is now in O0
        // and save the result to some temp
        int this_val_temp = m_symtab.getTemp(4);
        retval.setLocation(this_val_temp);
        aw.storeLocal(Register.O0, this_val_temp);

        return retval;

    }


    private Expr assign(Expr into, Expr from) {

//         System.out.println("Calling assign");
//         System.out.println("into is " + into.toString());
//         System.out.println("from is " + from.toString());

        if (from.getEquivType() instanceof ArrayType && into.getEquivType() instanceof PointerType) {

            int intoAddrTemp = m_symtab.getTemp(4);
            into.putAddrInto(intoAddrTemp, aw, m_symtab);

            int fromAddrTemp = m_symtab.getTemp(4);
            from.putAddrInto(fromAddrTemp, aw, m_symtab);

            aw.loadLocal(fromAddrTemp, Register.L0);
            aw.loadLocal(intoAddrTemp, Register.L1);
            aw.write("\t" + "st\t" + Register.L0 + ", [" + Register.L1 + "]");
            
            return into;

        } if (into.getEquivType() instanceof StructType) {

            StructType type = (StructType) into.getEquivType();
            int size = type.size();


            int from_addr_temp = m_symtab.getTemp(4);
            from.putAddrInto(from_addr_temp, aw, m_symtab);

            int into_addr_temp = m_symtab.getTemp(4);
            into.putAddrInto(into_addr_temp, aw, m_symtab);

            // load address of source into L0
            aw.loadLocal(from_addr_temp, Register.L0);

            // load address of dest into L1
            aw.loadLocal(into_addr_temp, Register.L1);

            for (int i = 0; i < size; i += 4) {

                if (i != 0) {
                    aw.write("\t" + "add\t" + Register.L0 + ", " + 4 + ", " + Register.L0);
                    aw.write("\t" + "add\t" + Register.L1 + ", " + 4 + ", " + Register.L1);
                }

                aw.write("\t" + "ld\t" + "[" + Register.L0 + "], " + Register.L2);
                aw.write("\t" + "st\t" + Register.L2 + ", [" + Register.L1 + "]");
                
            }

            /// \todo this is not right, since we're making this
            /// return an lval.  We can't use an NCRV, since it
            /// expects its value to be present on the stack, which is
            /// not necessarily the case with struct assignment (such
            /// as if you're assigning one global struct to another).
            /// Anyhow, this WNBT for project 2.
            return from;

        } else if (into.getEquivType() instanceof IncompleteStructType) {

            IncompleteStructType type = (IncompleteStructType) into.getEquivType();

            int from_addr_temp = m_symtab.getTemp(4);
            from.putAddrInto(from_addr_temp, aw, m_symtab);

            int into_addr_temp = m_symtab.getTemp(4);
            into.putAddrInto(into_addr_temp, aw, m_symtab);

            aw.loadLocal(into_addr_temp, Register.O0);
            aw.loadLocal(from_addr_temp, Register.O1);
            aw.write("\t" + "set\t" + "SIZEOF_" + into.getEquivType().getName() + ", " + Register.O2);
            aw.write("\t" + "call\t" + "memmove");
            aw.write("\t" + "nop");

            return from;

        } else if (into.getEquivType() instanceof FunctionType && from.getType().untypedef() instanceof FunctionType) 
        {

            AbstractFunctionPointer lhs = (AbstractFunctionPointer) into;
            Callable rhs = (Callable) from;
            rhs.assignInto(lhs, aw, m_symtab);
            return rhs;
        } else {

            aw.writeComment("Entering MyParser.assign, misc section");



            int from_value_temp = m_symtab.getTemp(4);
            aw.writeComment("Putting from_value into " + from_value_temp);
            from.putValueInto(from_value_temp, aw, m_symtab);

            int into_addr_temp = m_symtab.getTemp(4);
            aw.writeComment("Putting into_addr into " + into_addr_temp);
            into.putAddrInto(into_addr_temp, aw, m_symtab);

            aw.writeComment("Loading from_value into %f0");
            aw.loadLocal(from_value_temp, Register.F0);

            if (from.getType().untypedef() instanceof IntType && into.getType().untypedef() instanceof FloatType)
                aw.write("\t" + "fitos\t" + Register.F0 + ", " + Register.F0);

            // load the dest addr into g2
            aw.writeComment("loading destination address into %g2");
            aw.loadLocal(into_addr_temp, Register.G2);

            // store f0 into [g2]
            aw.write("\t" + "st\t" + Register.F0 + ", [" + Register.G2 + "]");
            aw.write("");

            NCRV retval = new NCRV(((MLV)into).getType());

            int this_val_temp = m_symtab.getTemp(4);
            aw.writeComment("Writing the result of this assignment into " + this_val_temp);
            retval.setLocation(this_val_temp);
            aw.storeLocal(Register.F0, this_val_temp);

            aw.writeComment("Leaving MyParser.assign, misc section");

            return retval;

        }

    }
    
}
