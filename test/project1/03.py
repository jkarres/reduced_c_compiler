# testing stuff from check #3

# todo

# need to test these things both for stand-alone assignment and also
# for initialization


from test import run

# testing trying to assign stuff to a non modifiable L value

# assigning into literal
def test_01() :
    o, e = run('function : void f() { 3 = 4;}')
    return 'Left-hand operand is not assignable (not a modifiable L-value).' in o

# into const 'variable'
def test_02() :
    o, e = run('function : void f() { const int i = 3; i = 4;}')
    return 'Left-hand operand is not assignable (not a modifiable L-value).' in o

# good assignment
def test_03() :
    o, e = run('function : void f() { int i = 3; i = 4;}')
    return 'success' in o

# good assignment
def test_04() :
    o, e = run('''
    function : void f() {
      int[3] ia;
      ia[2] = 3;
    }''')
    return 'success' in o

# assignment into retval
def test_05() :
    o, e = run('''
    function : int i() { return 42; }
    function : void f() { i() = 32; }
    ''')
    return 'Left-hand operand is not assignable (not a modifiable L-value).' in o

# good assignmentt
def test_06() :
    o, e = run ('''
    function : float *f() { float f; return &f; }
    function : void g() { float f2; f2 = *f(); }
    ''')
    return 'success' in o

# assignment into function name
def test_07() :
    o, e = run('''
    function : void f() { }
    function : void h() { }
    function : void g() { f = g; }
    ''')
    return 'Left-hand operand is not assignable (not a modifiable L-value).' in o

# assignment into array name
def test_08() :
    o, e = run('''
    int[5] ia;
    int[5] ia2;
    function : void f() { ia = ia2; }
    ''')
    return 'Left-hand operand is not assignable (not a modifiable L-value).' in o

def test_08a() :
    o, e = run('''
    typedef int[5] IA;
    IA ia;
    IA ia2;
    function : void f() { ia = ia2; }
    ''')
    return 'Left-hand operand is not assignable (not a modifiable L-value).' in o

    
# assignment into addr-of result
def test_09() :
    o, e = run('''
    function : void f() {
      float a, b;
      &a = &b;
    }''')
    return 'Left-hand operand is not assignable (not a modifiable L-value).' in o

# assignment into result of type cast
def test_10() :
    o, e = run('''
    function : void f() {
      float f;
      int i;
      (float) i = f;
   }''')
    return 'Left-hand operand is not assignable (not a modifiable L-value).' in o

# assignment into struct
def test_11():
    o, e = run ('''
    structdef S { int a; float b; };
    S s1;
    S s2;
    function : void f() { s2 = s1; }
    ''')
    return 'success' in o

# assignment into struct member
def test_12() :
    o, e = run('''
    structdef S { float a; int** b; };
    function : void f() { S s; int** ppi; s.b = ppi; }
    ''')
    return 'success' in o

# assingment into struct member
def test_13() :
    o, e = run ('''
    structdef S { float a; int** b; };
    function : void f() {
        S s;
        S* ps;
        int** ppi;
        ps = &s;
        ps->b = ppi;
    }
    ''')
    return 'success' in o

# multiple failures
def test_14() :
    o, e = run (''' int a = 1 = 2 = 3 = 4;''');
    return 'Left-hand operand is not assignable (not a modifiable L-value).' in o

#
#
# check 3b - type conflict
#

# unassignable
def test_15() :
    o, e = run('function : void f() { float f; int i; i = f; }')
    return 'Value of type float not assignable to variable of type int.' in o

# assignable
def test_16() :
    o, e = run('function : void f() { float f; int i; f = i; }')
    return 'success' in o

# assignable
def test_17() :
    o, e = run('''
    typedef int I;
    typedef int II;
    typedef II III;
    function : void f() { I i; III iii; iii = i; }
    ''')
    return 'success' in o

# good
def test_18():
    o, e = run('''
    typedef int * PI;

    typedef int I;
    typedef I* OPI;

    function : void f() {
      PI pi;
      OPI opi;
      pi = opi;
    }''')
    return 'success' in o

# pointer type mismatch
def test_19():
    o, e = run('''
    function : void f() { int * pi; int ** ppi; ppi = pi; }''')
    return 'Value of type int* not assignable to variable of type int**.' in o

# point type mismatch
def test_20() :
    o, e = run('''
    structdef S { int i; };
    typedef S* PS;
    function : void f() { int * pi; PS ps; ps = pi; }
    ''')
    return 'Value of type int* not assignable to variable of type PS.' in o
    

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
