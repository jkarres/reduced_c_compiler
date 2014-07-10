from test import run

# todo : what if two paramaters with the same name?

# todo : const float f = 123;  is this really a float?

## function pointers


# good funcptr definition, init to func name, & call, return type is void
def test_01():
    o,e = run('''
    function : void f(int i) { }
    function : void g() {
      funcptr : void (int p1) fp = f;
      fp(42);
    }''')
    return 'success' in o
      
# good funcptr definition, init to func name, & call, return type is not void
def test_02():
    o,e = run('''
    structdef S { int i;};
    function : S makes(int j) {
      S s;
      s.i = j;
      return s;
    }
    function : S f() {
      funcptr : S (int k) fp = makes;
      return fp(13);
    }''')
    return 'success' in o

# good funcptr definition, init to other funcptr, & call, return type is void
def test_03():
    o,e = run('''
    funcptr : void (int i, int j) fp;
    function : void m() {
      funcptr : void (int j, int k) fptwo = fp;
      fptwo(10, 11);
    }''')
    return 'success' in o

# todo : "Argument passed to reference parameter float &g (type float)
# is not a modifiable L-value." is being given in the old (incorrect)
# version of this test.  This looks wrong.

# good funcptr definition, init to other funcptr, & call, return type
# is not void
def test_04():
    o,e = run('''
    funcptr : float (int i, float& f) fp;
    function : float m() {
      funcptr : float (int j, float& g) fptwo = fp;
      float afloat;
      return fptwo(10, afloat);
    }''')
    return 'success' in o

# good funcptr definition, init to func name, & call, return type is
# void, using typedefs
def test_05():
    o,e = run('''
    function : void donothing() { }
    function : void call(funcptr : void () f) {
      f();
    }
    funcptr : void (funcptr : void () h) g = call;
    function : int main() {
      g(donothing);
      return 0;
    }''')
    return 'success' in o
    
# good funcptr definition, init to func name, & call, return type is
# not void, using typedefs
def test_06():
    o,e = run('''
    function : int add(int a, int b) { return a + b; }
    function : void main() {
      funcptr : int (int p, int q) fp = add;
      int i = add(5, 6);
    }''')
    return 'success' in o
      
# good funcptr definition, init to other funcptr, & call, return type
# is void, using typedefs
def test_07():
    o,e = run('''
    function : void multiply(int a, int b, int & z) {
      z = a * b;
    }
    function : void main() {
      funcptr : void (int a, int b, int &c) fpone = multiply;
      funcptr : void (int x, int y, int &z) fptwo = fpone;
      int product;
      fptwo(10, 11, product);
    }''')
    return 'success' in o

# good funcptr definition, init to other funcptr, & call, return type
# is not void, using typedefs
def test_08():
    o,e = run('''
    typedef int I;
    typedef I II;
    typedef funcptr : I (int a, II & b) FPTYPEONE;
    typedef funcptr : II (I y, int & z) FPTYPETWO;
    function : int copy(I src, II & dest) {
      dest = src;
      return dest;
    }
    FPTYPEONE ptrtocopy = copy;
    FPTYPETWO nextptr = ptrtocopy;
    function : II main() {
      int n;
      I o;
      II p;
      o = nextptr(n, p);
      return o;
    }''')
    return 'success' in o

# good funcptr def, init/assign to NULL
def test_09():
    o,e = run('''
    typedef funcptr : int (float f) FTOI;
    FTOI fp = NULL;
    ''')
    return 'success' in o

# assignment/init with wrong return type (one void, one not)
def test_10():
    o,e = run('''
    function : void level(float & f) {
      f = (int) f;
    }
    funcptr : int (float &   f) fp = level;
    ''')
    return 'Initialization value of type funcptr : void (float &f) not assignable to constant/variable of type funcptr : int (float &f).' in o

def test_11():
    o,e = run('''
    function : int level(float f) { return (int) f; }
    funcptr : void (float f) fp = level;
    ''')
    return 'Initialization value of type funcptr : int (float f) not assignable to constant/variable of type funcptr : void (float f).' in o

# assignment/init with wrong return type (both non-void)
def test_11():
    o,e = run('''
    function : int level(float f) { return (int) f; }
    funcptr : int (float & f) fp = level;
    ''')
    return 'Initialization value of type funcptr : int (float f) not assignable to constant/variable of type funcptr : int (float &f)' in o

# assignment/init with wrong number of params
def test_11():
    o,e = run('''
    function : int level(float f) { return (int) f; }
    funcptr : int (float f, float f2) fp = level;
    ''')
    return 'Initialization value of type funcptr : int (float f) not assignable to constant/variable of type funcptr : int (float f, float f2).' in o

def test_12():
    o,e = run('''
    function : int add(int i1, int i2) { return i1 + i2; }
    funcptr : int (int i1) fp = add;
    ''')
    return 'Initialization value of type funcptr : int (int i1, int i2) not assignable to constant/variable of type funcptr : int (int i1).' in o
              

# assignment/init with param differing in name (okay!)
def test_13():
    o,e = run('''
    typedef int I;
    typedef int J;
    typedef J K;
    function : int add(int i1, int i2) { return i1 + i2; }
    funcptr : I (J first, K second) fp = add;
    ''')
    return 'success' in o

# assignment/init with param differing in reference/value choice (bad)
def test_14():
    o,e = run('''
    function : void f1(bool & b) { }
    funcptr : void (bool b) fp = f1;
    ''')
    return 'Initialization value of type funcptr : void (bool &b) not assignable to constant/variable of type funcptr : void (bool b).' in o

# assignment/init with param differing in type (bad)
def test_15():
    o,e = run('''
    function : int findanswer() { return 42; }
    funcptr : float () fp = findanswer;
    ''')
    return 'Initialization value of type funcptr : int () not assignable to constant/variable of type funcptr : float ().' in o

# assignment/init with param differing in type, but typedef equiv (okay)
def test_16():
    o,e = run('''
    // equiv to bool
    typedef bool A;
    typedef A B;
    typedef B C;
    typedef bool D;

    // equiv to bool*
    typedef A * E;
    typedef B * F;
    typedef C * G;

    // equiv to bool **
    typedef bool ** H;
    typedef G * I;
    typedef E * J;

    function : H bigsig(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j) {
      return j;
    }
    funcptr : I (D d, C c, B b, A a, G g, F f, E e, J j, I i, H h) fp = bigsig;
    ''')
    return 'success' in o

# assignment with multiple params wrong (should be just one error?)
def test_17():
    o,e = run('''
    function : bool f(int i, float f) { return false; }
    funcptr : bool (int * i, float * f) fp = f;
    ''')
    return o == 'Error, "": \n  Initialization value of type funcptr : bool (int i, float f) not assignable to constant/variable of type funcptr : bool (int* i, float* f).\nCompile: failure.\n'

# funcptr == NULL
def test_18():
    o,e = run('''
    funcptr : void () fp;
    function : bool f() {
      return fp == NULL;
    }''')
    return 'success' in o

# NULL == funcptr
def test_19():
    o,e = run('''
    funcptr : void () fp;
    function : bool f() {
      return NULL == fp;
    }''')
    return 'success' in o

# funcptr != NULL
def test_20():
    o,e = run('''
    funcptr : void () fp;
    function : bool f() {
      return fp != NULL;
    }''')
    return 'success' in o

# NULL != funcptr
def test_21():
    o,e = run('''
    funcptr : void () fp;
    function : bool f() {
      return NULL != fp;
    }''')
    return 'success' in o


# check the way funcptr typename is printed out; example using typedef
# name
def test_22():
    o,e = run('''
    typedef funcptr : int (float f) FTOI;
    FTOI fp1;
    typedef funcptr : float (int i) ITOF;
    ITOF fp2;
    bool b = fp1 == fp2;
    ''')
    return o == 'Error, "(stdin)": \n  Incompatible types to operator ==:\n    FTOI,\n    ITOF;\n  both must be of equivalent pointer type.\nCompile: failure.\n'

# check the way funcptr typename is printed out; example using
# "funcptr: ..." form
def test_23():
    o,e = run('''
    funcptr : int (float f) fp1;
    funcptr : float (int i) fp2;
    bool b = fp1 == fp2;
    ''')
    return o == 'Error, "(stdin)": \n  Incompatible types to operator ==:\n    funcptr : int (float f),\n    funcptr : float (int i);\n  both must be of equivalent pointer type.\nCompile: failure.\n'

# check the way function type is printed
def test_24():
    o,e = run('''
    function : int f1 (float f) { return 42; }
    function : float f2 (int i) { return 42.0; }
    bool b = f1 == f2;
    ''')
    return o == 'Error, "(stdin)": \n  Incompatible types to operator: funcptr : int (float f) == funcptr : float (int i);\n  both must be numeric, or both equivalent to bool.\nCompile: failure.\n'

# check that a funcptr that was defined using a typedef has the
# typedef name printed out in error messages, and not the "funcptr:
# ..." form : handled in test_22

# miscellaneous bad funcptr initialization/assignments
def test_25():
    o,e = run(''' funcptr : void () fp = 27; ''')
    return o == 'Error, "": \n  Initialization value of type int not assignable to constant/variable of type funcptr : void ().\nCompile: failure.\n'

# make sure reference to typedef is compatible with reference to base
def test_26():
    o,e = run('''
    typedef bool B;
    typedef bool C;
    typedef int I;
    typedef int J;
    typedef int K;
    typedef int L;
    typedef int M; 
    function : B lessthan(I & i, J & j) { return i < j; }
    funcptr : C (K & k, int & l) fp = lessthan;
    L l;
    M m;
    bool b = fp(l, m);
    ''')
    return 'success' in o


# also need to test whether misusing function pointers gives you the
# same errors as misusing function names
#
# todo: are the param names printing out correctly?
def test_27():
    o,e = run('''
    funcptr : void (int & i, float & f) fp;
    function : void f() {
      fp(42, true);
    }''')
    return o == 'Error, "(stdin)": \n  Argument passed to reference parameter i (type int) is not a modifiable L-value.\nError, "(stdin)": \n  Argument of type bool not equivalent to reference parameter f, of type float.\nCompile: failure.\n'

def run_all():
    fails = []
    for f in [x for x in globals() if x.startswith('test_')]:
        print "running", f
        if not globals()[f]():
            fails.append('failed %s' % f)
    fails.sort()
    for fail in fails:
        print "!!!!!!!!!!", fail



if __name__ == '__main__':
    run_all()
