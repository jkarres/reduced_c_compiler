from test import run

# good: array [] int
def test_01():
    o,e = run('''
    bool[10] ba;
    function : void f() {
      ba[0] = true;
    }''')
    return 'success' in o

# good: array [] int, w/typedef
def test_02():
    o,e = run('''
    typedef int I;
    typedef bool[5] BA;
    function : void f() {
      BA ba;
      ba[(I)3.1] = false;
    }''')
    return 'success' in o

# good : array [] expr-eval-to-int
def test_03():
    o,e = run('''
    function : int* intfunc() { int a; return &a; }
    function : void f() {
      float[5] fa;
      fa[*intfunc()] = 20.0/7.0;
    }''')
    return 'success' in o

# good: pointer [] int
def test_04():
    o,e = run('''
    float * pi;
    function : void f () {
      pi[3] = 9.0;
    }
    ''')
    return 'success' in o

# good: pointer [] int, w/typedef
def test_05():
    o,e = run('''
    typedef int * PI;
    PI pi;
    typedef int I;
    function : void f() {
      I i;
      pi[i] = 3;
    }''')
    return 'success' in o

# good : point [] expr-eval-to-int
def test_06():
    o,e = run('''
    float * pf;
    function : int fi () { return 7; }
    function : void f() {
      pf[fi()] = 2.0;
    }''')
    return 'success' in o

# bad: other [] int
def test_07():
    o,e = run('''
    structdef S { int i; };
    function : void f() {
      S s;
      s[0] = 7;
    }''')
    return 'Type of expression referenced by array subscript (S) is not of array or pointer type.' in o

def test_09():
    o,e = run('''
    int i;
    function : void f() {
      i[3] = 4;
    }''')
    return 'Type of expression referenced by array subscript (int) is not of array or pointer type.' in o

# bad: other [] int, w/typedef
def test_08():
    o,e = run('''
    structdef S { int i; };
    typedef S TDS;
    function : void f() {
      TDS s;
      s[0] = 7;
    }''')
    return 'Type of expression referenced by array subscript (TDS) is not of array or pointer type.' in o

# bad: array [] non-int
def test_10():
    o,e = run('''
    bool[10] ba;
    function : void f() {
      ba[true] = false;
    }''')
    return 'Type of index expression in array reference (bool) not equivalent to int.' in o

# bad : pointer [] non-int; what should the message be?
def test_11():
    o,e = run('''
    bool * pb;
    function : void f() {
      pb[true] = false;
    }''')
    return 'Type of index expression in array reference (bool) not equivalent to int.' in o

# bad: const outside array bounds (too big)
def test_12():
    o,e = run('''
    float[5] fa;
    function : void f() {
      fa[5] = 3.1;
    }''')
    return 'Index value of 5 is outside legal range [0,5).' in o

# bad: const outside array bounds (too small)
def test_13():
    o,e = run('''
    float[5] fa;
    function : void f() {
      fa[-1] = 3.1;
    }''')
    return 'Index value of -1 is outside legal range [0,5).' in o

# good : pointer with -1 index
def test_14():
    o,e = run('''
    float* pf;
    function : void f() {
      pf[-1] = 3.1;
    }''')
    return 'success' in o
    

# good : multiple []s
def test_15():
    o,e = run('''
    typedef int I;
    typedef I[5] IA;
    typedef IA[2] IAA;
    function : void f() {
      IAA iaa;
      iaa[0][0] = 3;
    }''')
    return 'success' in o

# bad : multiple []s
def test_16():
    o,e = run('''
    typedef int I;
    typedef I[5] IA;
    typedef IA[2] IAA;
    function : void f() {
      IAA iaa;
      iaa[2][0] = 3;
    }''')
    return 'Index value of 2 is outside legal range [0,2).' in o

def test_17():
    o,e = run('''
    typedef int I;
    typedef I[5] IA;
    typedef IA[2] IAA;
    function : void f() {
      IAA iaa;
      iaa[1][5] = 3;
    }''')
    return 'Index value of 5 is outside legal range [0,5).' in o

def test_18():
    o,e = run('''
    typedef int I;
    typedef I[5] IA;
    typedef IA[2] IAA;
    function : void f() {
      IAA iaa;
      iaa[1][-2] = 3;
    }''')
    return 'Index value of -2 is outside legal range [0,5).' in o

def test_19():
    o,e = run('''
    typedef int I;
    typedef I[5] IA;
    typedef IA[2] IAA;
    function : void f() {
      IAA iaa;
      iaa[1][0-2] = 3;
    }''')
    return 'Index value of -2 is outside legal range [0,5).' in o


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
