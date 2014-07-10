from test import run

# todo : sizeof funcname ?  does that makes anysense?

# todo : add cases of structs that also have functions


## generate error if operand not a type and not addressable

# operand a literal -> error
def test_01():
    o,e = run('''
    const int i = sizeof(342);
    ''')
    return "Invalid operand to sizeof. Not a type or not addressable." in o

# operand a const variable -> okay
def test_02():
    o,e = run('''
    const int i = 43;
    const int j = sizeof(i);
    ''')
    return 'success' in o

# operand a nonconst variable -> okay
def test_03():
    o,e = run('''
    int i = 432;
    const int j = sizeof(i);
    ''')
    return 'success' in o

# operand an rval expr -> error
def test_04():
    o,e = run('''
    function : bool getfalse() { return false; }
    const int i = sizeof(getfalse());
    ''')
    return "Invalid operand to sizeof. Not a type or not addressable." in o

# operand an lval expr -> okay
def test_05():
    o,e = run('''
    structdef S { int i; float f;};
    S * p;
    const int i = sizeof(*p);
    ''')
    return 'success' in o

## check for proper sizes

# int = 4
def test_06():
    o,e = run('''
    int i;
    const int j = sizeof(i);
    ''')
    return 'success' in o and '<IntValue>4</IntValue>' in e

def test_08():
    o,e = run(''' const int j = sizeof(int); ''')
    return 'success' in o and '<IntValue>4</IntValue>' in e

# float = 4
def test_09():
    o,e = run('''
    int i;
    const int j = sizeof(i);
    ''')
    return 'success' in o and '<IntValue>4</IntValue>' in e

def test_10():
    o,e = run('const int j = sizeof(float);')
    return 'success' in o and '<IntValue>4</IntValue>' in e

# bool = 4
def test_11():
    o,e = run('''
    bool i;
    const int j = sizeof(i);
    ''')
    return 'success' in o and '<IntValue>4</IntValue>' in e

def test_12():
    o,e = run('const int j = sizeof(bool);')
    return 'success' in o and '<IntValue>4</IntValue>' in e

# char = 1 (not tested)
def test_13():
    o,e = run('''
    char i;
    const int j = sizeof(i);
    ''')
    return 'success' in o and '<IntValue>1</IntValue>' in e

def test_14():
    o,e = run('const int j = sizeof(char);')
    return 'success' in o and '<IntValue>1</IntValue>' in e

# normal pointer = 4
def test_15():
    o,e = run('''
    bool ***** pb;
    const int j = sizeof(pb);
    ''')
    return 'success' in o and '<IntValue>4</IntValue>' in e

def test_16():
    o,e = run('const int j = sizeof(float*);')
    return 'success' in o and '<IntValue>4</IntValue>' in e

# function pointer = 4 (not tested)
def test_17():
    o,e = run('''
    funcptr : void (int i, float f, bool b) pf;
    const int j = sizeof(pf);
    ''')
    return 'success' in o and '<IntValue>4</IntValue>' in e

def test_18():
    o,e = run('const int j = sizeof(funcptr: void ());')
    return 'success' in o and '<IntValue>4</IntValue>' in e

# functions?  not defined and not tested

# structs
def test_19():
    o,e = run('''
    structdef S {
      int i;
      float f;
      bool b;
    };
    S s;
    const int j = sizeof(s);
    ''')
    return 'success' in o and '<IntValue>12</IntValue>' in e

def test_19a():
    o,e = run('''
    structdef S {
      int i;
      float f;
      bool b;
      function : float getsum() {
        return this.i + this.f;
      }
    };
    S s;
    const int j = sizeof(s);
    ''')
    return 'success' in o and '<IntValue>12</IntValue>' in e


def test_20():
    o,e = run('''
    structdef S {
      int i;
      float f;
      bool b;
    };
    const int j = sizeof(S);
    ''')
    return 'success' in o and '<IntValue>12</IntValue>' in e

def test_21():
    o,e = run('''
    structdef S {
      int i;
      float f;
      bool b;
    };
    S s;
    S* ps;
    const int j = sizeof(*ps);
    ''')
    return 'success' in o and '<IntValue>12</IntValue>' in e

def test_22():
    o,e = run('''
    structdef S {
      int i;
      float f;
      bool b;
    };
    S s;
    S* ps;
    const int j = sizeof(ps);
    ''')
    return 'success' in o and '<IntValue>4</IntValue>' in e
    
def test_23():
    o,e = run('''
    structdef S { int i; float f; float g; };
    structdef T { S s1; S s2; bool b; };
    const int j = sizeof(T);
    ''')
    return 'success' in o and '<IntValue>28</IntValue>' in e

def test_23a():
    o,e = run('''
    structdef S {
      int i;
      float f;
      float g;
      function : float sum() {
        return this.i + this.f + this.g;
      }
    };
    structdef T {
      S s1;
      S s2;
      bool b;
      function : float sum() {
        return this.s1.sum() + this.s2.sum() + (int)this.b;
      }
    };
    const int j = sizeof(T);
    ''')
    return 'success' in o and '<IntValue>28</IntValue>' in e

# arrays
def test_24():
    o,e = run('''
    bool[5] ba;
    const int j = sizeof(ba);
    ''')
    return 'success' in o and '<IntValue>20</IntValue>' in e

def test_25():
    o,e = run(''' const int j = sizeof(float[10]); ''')
    return 'success' in o and '<IntValue>40</IntValue>' in e

def test_26():
    o,e = run('''
    structdef S { int i; float f; float g; };
    structdef T { S s1; S s2; bool b; };
    T[10] ta;
    const int j = sizeof(ta);
    ''')
    return 'success' in o and '<IntValue>280</IntValue>' in e

def test_27():
    o,e = run('''
    structdef S { int i; float f; float g; };
    structdef T { S s1; S s2; bool b; };
    const int j = sizeof(T[10]);
    ''')
    return 'success' in o and '<IntValue>280</IntValue>' in e

# typedefs
def test_28():
    o,e = run('''
    structdef S { int i; float f; float g; };
    typedef S SS;
    structdef T { SS s1; S s2; bool b; };
    typedef T TT;
    typedef TT[10] TTARRAY;
    const int j = sizeof(TTARRAY);
    ''')
    return 'success' in o and '<IntValue>280</IntValue>' in e

def test_29():
    o,e = run('''
    typedef int I;
    structdef S { I[10] ia; };
    typedef S SS;
    SS ss;
    const int j = sizeof(ss);
    ''')
    return 'success' in o and '<IntValue>40</IntValue>' in e

## check that result of sizeof is a constant rval
def test_30():
    o,e = run(''' const int j = sizeof(sizeof(int));''')
    return "Invalid operand to sizeof. Not a type or not addressable." in o

# trying const function pointers fails, but check 18b in assignment
# description says constant function pointers will not be tested.
## def test_31():
##     o,e = run('''
##     function : void f() { }
##      funcptr : void () fp = f;
##     ''')
##     print o

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
