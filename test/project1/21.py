from test import run

# fail on literal
def test_01():
    o,e = run(''' int * pi = &42; ''')
    return 'Non-addressable argument of type int to address-of operator.' in o

# ok on const variable
def test_02():
    o,e = run('''
    const bool b = false;
    bool * pb = &b;
    ''')
    return 'success' in o

# ok on normal variable
def test_03():
    o,e = run('''
    bool b = false;
    bool * pb = &b;
    ''')
    return 'success' in o

# fail on func retval
def test_04():
    o,e = run('''
    function : bool f() { return true; }
    bool * pb = &(f());
    ''')
    return 'Non-addressable argument of type bool to address-of operator.' in o

# work on struct member
def test_05():
    o,e = run('''
    structdef S { int i; bool b; };
    S s;
    int * pi = &(s.i);
    ''')
    return 'success' in o

# work on array member
def test_05():
    o,e = run('''
    typedef int[10] IA;
    IA ia;
    int * pi = &ia[3];
    ''')
    return 'success' in o

# cannot take address of result
def test_06():
    o,e = run('''
    int i;
    int ** ppi = &(&i);
    ''')
    return 'Non-addressable argument of type int* to address-of operator.' in o

# cannot assign into result
def test_07():
    o,e = run('''
    int i;
    function : void f() {
      &i = (int*) 57;
    }''')
    return 'Left-hand operand is not assignable (not a modifiable L-value).' in o

# check that & yields pointer of appropriate type
def test_08():
    o,e = run('''
    bool b;
    float * pf = &b;
    ''')
    return 'Initialization value of type bool* not assignable to constant/variable of type float*.' in o

# dereferencing result is always modifiable lvalue
def test_09():
    o,e = run('''
    const int i = 987;
    function : void f() {
      *&i = 1234;
    }''')
    return 'success' in o

# fail on &funcname
def test_10():
    o,e = run('''
    function : void f() { }
    funcptr : void () fp = &f;
    ''')
    return 'Non-addressable argument of type funcptr : void () to address-of operator.' in o

# doesn't really belong here, but what the heck
def test_11():
    o,e = run('''
    function : void f () { }
    function : void g () { }
    function : void h () {
      g = f;
    }''')
    return 'Left-hand operand is not assignable (not a modifiable L-value).' in o

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
