from test import run

## check equ and neq for pointers

# test successful ptr equ ptr
def test_01():
    o,e = run('''
    function : bool f () {
      int * pia, pib;
      return pia == pib;
    }''')
    return 'success' in o

# test successful ptr equ null
def test_02():
    o,e = run('''
    function : bool f() {
      int* pi;
      return pi == NULL;
    }''')
    return 'success' in o

# test successful null equ ptr
def test_03():
    o,e = run('''
    function : bool f() {
      int* pi;
      return NULL == pi;
    }''')
    return 'success' in o

# test successful null equ null, check const expr
def test_04():
    o,e = run(''' const bool b = (NULL == NULL); ''')
    return 'success' in o and '<BooleanValue>true</BooleanValue>' in e

# test successful ptr equ ptr
def test_05():
    o,e = run('''
    function : bool f () {
      int * pia, pib;
      return pia != pib;
    }''')
    return 'success' in o

# test successful ptr equ null
def test_06():
    o,e = run('''
    function : bool f() {
      int* pi;
      return pi != NULL;
    }''')
    return 'success' in o

# test successful null equ ptr
def test_07():
    o,e = run('''
    function : bool f() {
      int* pi;
      return NULL != pi;
    }''')
    return 'success' in o


# test successful null equ null, check const expr
def test_08():
    o,e = run(''' const bool b = (NULL != NULL); ''')
    return 'success' in o and '<BooleanValue>false</BooleanValue>' in e

# test unsuccessful ptr equ ptr, different pointee type
def test_09():
    o,e = run('''
    function : bool f() {
      int * pi;
      float * pf;
      return pi == pf;
    }''')
    return '''Incompatible types to operator ==:
    int*,
    float*;
  both must be of equivalent pointer type.''' in o and 'Return required in function, none found.' not in o

# test successful ptr equ typedef = pointer (check different levels
# where the typedef could be)
def test_10():
    o,e = run('''
    typedef int * PI;
    function : bool f() {
      int * pione;
      PI pitwo;
      return pione == pitwo;
    }''')
    return 'success' in o

def test_11():
    o,e = run('''
    typedef float F;
    typedef F FF;
    function : bool f() {
      float * * fpone;
      F * * fptwo;
      return fpone == fptwo;
    }''')
    return 'success' in o

## check pre/post incr/decr

# good pre incr
def test_12():
    o,e = run('''
    typedef int I;
    function : I f() {
      I * pi;
      return *++pi;
    }''')
    return 'success' in o

# good post incr
def test_13():
    o,e = run('''
    typedef int I;
    function : I f() {
      I * pi;
      return *pi++;
    }''')
    return 'success' in o

# good pre decr
def test_14():
    o,e =  run('''
    function : void f() {
      bool * pb;
      --pb;
    }''')
    return 'success' in o

# good post decr
def test_15():
    o,e =  run('''
    function : void f() {
      bool * pb;
      pb--;
    }''')
    return 'success' in o

## check ptr and null assignment compatibility
def test_16():
    o,e = run(''' int * pi = NULL; ''')
    return 'success' in o
    
def test_17():
    o,e = run('''
    function : int *** f() {
      return NULL;
    }''')
    return 'success' in o


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
