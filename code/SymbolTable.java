//---------------------------------------------------------------------
//
//---------------------------------------------------------------------
import java.util.*;


class SymbolTable
{
    //----------------------------------------------------------------
    //	Instance variables.
    //----------------------------------------------------------------
	private Stack<Scope> scopes;
	private int	m_nLevel;
	private GlobalScope m_scopeGlobal;
    private FunctionSTO currentFunc;
    private MyParser parser;
    private int initFuncTempSize = 0;

	public SymbolTable(MyParser p) {
		m_nLevel = 0;
		scopes = new Stack<Scope>();
		m_scopeGlobal = null;
        parser = p;
	}

    public void insertExtern(STO sto) {
        if (sto instanceof HasLocation) {
            ((HasLocation)sto).setLocation(new NonlocalMemoryLocation(sto.getName()));
        }
        scopes.peek().InsertLocal(sto);
        
    }

    public void insert(STO sto) {
        insert(sto, false);
    }

	public void insert(STO sto, boolean isStatic) {
		Scope scope = scopes.peek();
		scope.InsertLocal(sto);

        if (!(scope instanceof StructScope) && 
            sto instanceof HasLocation)
        {
            for (int i = scopes.size()-1; i > 0; --i) {
                Scope sc = scopes.elementAt(i);
                if (sc instanceof FunctionScope) {
                    ((FunctionScope)sc).allocate((HasLocation)sto, parser.aw, isStatic);
                    return;
                }
            }

            // if you're still here, there was no function scope, so
            // this goes into the global scope
            GlobalScope gs = (GlobalScope)scopes.elementAt(0);
            gs.allocate((HasLocation)sto, parser.directWriter, isStatic);

        }

	}

    public int getTemp(int spaceNeeded) {
        for (int i = scopes.size()-1; i > 0; --i) {
            Scope sc = scopes.elementAt(i);
            if (sc instanceof FunctionScope) {
                return ((FunctionScope)sc).allocate(spaceNeeded, parser.aw);
            }
        }

        // if you're here, then you're trying to allocate a temp for a
        // global initialization.
        initFuncTempSize += spaceNeeded;
        return initFuncTempSize;

    }

    public int getInitFuncTempSize() {
        return initFuncTempSize;
    }

	public STO accessGlobal(String strName) {
		return(m_scopeGlobal.access(strName));
	}


	public STO accessLocal(String strName) {
		Scope scope = scopes.peek();

		return(scope.accessLocal(strName));
	}


    // if directly in struct scope, return whether name is in that
    // struct.  otherwise return false;
    public boolean checkStructMember(String name) {
        Scope scope = scopes.peek();
        if (scope instanceof StructScope) {
            return ((StructScope)scope).accessMember(name) != null;
        } else {
            return false;
        }
    }

	public STO access(String strName) {
        for (int i = scopes.size()-1; i >= 0; --i)
        {
            Scope scope = scopes.get(i);
            STO stoReturn = null;
            if ((stoReturn = scope.access(strName)) != null)
                return stoReturn;
        }
		return null;
	}

    public IncompleteStructType getThisType() {
        for (int i = scopes.size()-1; i >= 0; --i) {
            Scope scope = scopes.get(i);
            if (scope instanceof StructScope) {
                StructScope ss = (StructScope)scope;
                return ss.getType();
            }
        }
        return null;
    }

    public void openOtherScope() {
        Scope scope = new OtherScope();
        assert m_scopeGlobal != null;
        scopes.push(scope);
        m_nLevel++;
    }

    public void openFunctionScope(FunctionSTO funcSto) {
        Expr func = funcSto;
        assert func.getType() instanceof FunctionType;
        currentFunc = funcSto;
        Scope scope = new FunctionScope(funcSto, scopes.peek() instanceof StructScope);
        assert m_scopeGlobal != null;
        scopes.push(scope);
        m_nLevel++;
    }

    public FunctionScope closeFunctionScope() {
        Scope rv = closeScope();
        assert rv instanceof FunctionScope;
        return (FunctionScope)rv;
    }

    public void openGlobalScope() {
        GlobalScope scope = new GlobalScope();
        assert m_scopeGlobal == null;
        m_scopeGlobal = scope;
        scopes.push(scope);
        m_nLevel++;
    }

    public void openForScope() {
        scopes.push(new ForScope());
        m_nLevel++;
    }


    public boolean inForScope() {
        for (int i = scopes.size()-1; i >= 0; --i)
            if (scopes.get(i) instanceof ForScope)
                return true;
        return false;
    }

    public void openStructScope(String name) {
        scopes.push(new StructScope(name));
        m_nLevel++;
    }

    public StructScope closeStructScope() {
        Scope s = closeScope();
        assert s instanceof StructScope;
        return (StructScope)s;
    }

    public boolean inStructScope() {
        return scopes.peek() instanceof StructScope;
    }

    public Scope getCurrentScope() {
        return scopes.peek();
    }

	public Scope closeScope() {
		Scope popped = scopes.pop();
		m_nLevel--;

        /// \todo no nested functions, right?
        if (popped instanceof FunctionScope) {
            currentFunc = null;
        }

        return popped;
	}

	public int getLevel() {
		return m_nLevel;
	}

    FunctionSTO getFunc() {
        return currentFunc;
    }

    boolean atGlobalScope() {
        return scopes.size() == 1;
    }

}
