from test import run

## extending all previous checks for (1) functions that return
## pointers and (2) structs, pointers, pointer dereferences

# good function returning pointer
def test_01():
    o,e = run('''
    function : float *pp() {
      float fl = 3.1415;
      return &fl;
    }
    function : void m() {
      float f = *pp();
    }''')
    return 'success' in o

# good pointer dereference
def test_02():
    o,e = run('''
    int i;
    int *pi = &i;
    function : void m() {
      int j = *pi;
    }''')
    return 'success' in o

# trying to dereference a non-pointer
def test_03():
    o,e = run('''
    function : void m() {
      int i;
      int j = *i;
    }''')
    return 'Incompatible type int to unary dereference operator *, pointer expected.' in o

# good (*ptr).f (ptr is a pointer to a struct)
def test_04():
    o,e = run('''
    structdef S { int i; };
    S s;
    S* ps = &s;
    int i = (*ps).i;
    ''')
    return 'success' in o

## testing arrow operator
# good arrow operator
def test_05():
    o,e = run('''
    structdef S { int i; };
    S s;
    S* ps = &s;
    int i = ps->i;
    ''')
    return 'success' in o

# bad arrow: lhs not a pointer to a struct
def test_06():
    o,e = run('''
    structdef S { int i; };
    S s;
    int i = s->i;
    ''')
    return 'Incompatible type S to operator ->, pointer to struct expected.' in o

# bad arrow: rhs not a field in the struct
def test_07():
    o,e = run('''
    structdef S { int i; };
    S s;
    int i = (&s)->j;
    ''')
    return 'Referenced field j not found in type S.' in o
    print o

## extending check 3 for pointers
# good pointer dereference on lhs of assign statement
def test_08():
    o,e = run('''
    function : void f() {
      int i;
      int * pi;
      *pi = 3;
    } ''')
    return 'success' in o

# bad pointer dereference on lhs of assign statement (wrong type)
def test_09():
    o,e = run('''
    function : void f() {
      int i;
      int * pi;
      *pi = 3.1415;
    }''')
    return 'Value of type float not assignable to variable of type int.' in o

## extending check 8 for illegal initialization of a pointer.
## Should probably also check for assignment too.

# good initialization to NULL
def test_09():
    o,e = run('''
    function : void f() {
      int * pi = NULL;
    }''')
    return 'success' in o

# good initialization to another pointer
def test_10():
    o,e = run('''
    function : void f() {
      int i = 43;
      int * pi = &i;
      int * opi = pi;
    }''')
    return 'success' in o

# good initialization to result of &
def test_11():
    o,e = run('''
    function : void f() {
      int i;
      int * pi = &i;
    }''')
    return 'success' in o

# good initialization to array name
def test_12():
    o,e = run('''
    function : void f() {
      float[5] f;
      float * fp = f;
    }''')
    return 'success' in o

# bad initialization to wrong pointer type
def test_13():
    o,e = run('''
    function : void f() {
      float * fp;
      int * ip = fp;
    }''')
    return 'Initialization value of type float* not assignable to constant/variable of type int*.' in o

# bad initialization to some other expr
# bad init (trying to take addr of rval)
def test_14():
    o,e = run('''
    function : void f() {
      int * pi = &(3+4);
    }''')
    return 'Non-addressable argument of type int to address-of operator.' in o

def test_15():
    o,e = run('''
    function : void f() {
      bool * pi = &(3+4);
    }''')
    return 'Non-addressable argument of type int to address-of operator.' in o

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
