from test import run

## should be able to cast from any type of expr

# casting a literal 
def test_01():
    o,e = run('''
    function : void f(int i) { }
    function : void m() {
      f((int)3.1415);
    }''')
    return 'success' in o

# casting a const var
def test_02():
    o,e = run('''
    function : void f(int i) { }
    const float pi = 3.1415;
    function : void m() {
      f((int)pi);
    }''')
    return 'success' in o

# casting a normal var
def test_03():
    o,e = run('''
    function : void f(int i) { }
    float pi = 3.1415;
    function : void m() {
      f((int)pi);
    }''')
    return 'success' in o

## should be able to cast into these types, and equivalent typedefs

# casting to int
def test_04():
    o,e = run('''
    function : int f(int i) { return 42; }
    int i = f((int)true);
    ''')
    return 'success' in o

def test_04a():
    o,e = run('''
    typedef int I;
    function : int f(int i) { return 42; }
    int i = f((I)true);
    ''')
    return 'success' in o
    

# casting to float
def test_05():
    o,e = run('''
    function : int f(float f) { return 42; }
    int i = f((float) true);
    ''')
    return 'success' in o

def test_05a():
    o,e = run('''
    typedef float F;
    typedef F FF;
    typedef float G;
    function : int f(G f) { return 42; }
    int i = f((FF) true);
    ''')
    return 'success' in o

# casting to bool
def test_06():
    o,e = run('''
    function : int f(bool f) { return 42; }
    int i = f((bool) 99);
    ''')
    return 'success' in o

# casting to pointer
def test_07():
    o,e = run('''
    function : int f(bool* pb) { return 42; }
    int i = f((bool*) 99);
    ''')
    return 'success' in o

def test_07a():
    o,e = run('''
    typedef bool B;
    typedef B* PB;
    typedef bool * PBOO;
    function : int f(PB pb) { return 42; }
    int i = f((PBOO) 99);
    ''')
    return 'success' in o

## invalid casts

# casting to none of the above
def test_08():
    o,e = run('''
    typedef int[5] IA;
    IA ia = (IA) 27;
    ''')
    return 'Invalid type cast. Type int to type IA is not supported.' in o
    
# casting from invalid source
def test_09():
    o,e = run('''
    function : void f() { }
    int i = (int)f;
    ''')
    return 'Invalid type cast. Type funcptr : void () to type int is not supported.' in o


## should yield an rval
def test_10():
    o,e = run('''
    function : void f(int & i) { }
    function : void m() {
      float pi = 3.1415;
      int * p = &((int)pi);
    }''')
    return 'Non-addressable argument of type int to address-of operator.' in o

## constant folding

# const bool -> int
def test_11():
    o,e = run('''
    const bool b = true;
    const int i = (int) b;
    ''')
    return 'success' in o and '<IntValue>1</IntValue>' in e

def test_12():
    o,e = run('''
    const bool b = false;
    const int i = (int) b;
    ''')
    return 'success' in o and '<IntValue>0</IntValue>' in e

# const bool -> float
def test_13():
    o,e = run('''
    const bool b = true;
    const float f = (float) b;
    ''')
    return 'success' in o and '<FloatValue>1.0</FloatValue>' in e

# const int -> bool
def test_14():
    o,e = run('''
    const bool b = false;
    const float f = (float) b;
    ''')
    return 'success' in o and '<FloatValue>0.0</FloatValue>' in e

# const int -> float
def test_15():
    o,e = run('''
    const int i = 37;
    const float f = (float) i;
    ''')
    return 'success' in o and '<FloatValue>37.0</FloatValue>' in e

# const float -> bool
def test_16():
    o,e = run('''
    const float f = 37.9;
    const bool b = (bool) f;
    ''')
    return 'success' in o and '<BooleanValue>true</BooleanValue>' in e
    
def test_17():
    o,e = run('''
    const float f = 0.0;
    const bool b = (bool) f;
    ''')
    return 'success' in o and '<BooleanValue>false</BooleanValue>' in e
    
# const float -> int
def test_18():
    o,e = run('''
    const float f = 342.7219;
    const int i = (int) f;
    ''')
    return 'success' in o and '<IntValue>342</IntValue>' in e

# const int -> ptr -> int
def test_19():
    o,e = run('''
    const int i = 27;
    const int j = (int)(float **) i;
    ''')
    return 'success' in o and '<CVar><value><IntValue>27</IntValue></value><name>j</name></CVar>' in e

def test_20():
    o,e = run('''
    typedef float** FPP;
    const int i = 27;
    const int j = (int)(FPP) i;
    ''')
    return 'success' in o and '<CVar><value><IntValue>27</IntValue></value><name>j</name></CVar>' in e

# const int -> ptr -> float
def test_21():
    o,e = run('''
    const int i = 987;
    const float f = (int)(char*)i;
    ''')
    return 'success' in o and '<FloatValue>987.0</FloatValue>' in e

# const int -> ptr -> bool
def test_22():
    o,e = run('''
    const int i = 0;
    const bool b = (bool)(bool**)i;
    ''')
    return 'success' in o and '<BooleanValue>false</BooleanValue>' in e

def test_23():
    o,e = run('''
    const int i = 990;
    const bool b = (bool)(bool**)i;
    ''')
    return 'success' in o and '<BooleanValue>true</BooleanValue>' in e

# const float -> ptr -> int
def test_24():
    o,e = run('''
    const float pi = 3.1415;
    const int i = (int)(bool********)pi;
    ''')
    return 'success' in o and '<IntValue>3</IntValue>' in e

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
