from test import run

# todo
# test multiple problems, make sure errors in correct order


# good function call
def test_00():
    o, e = run('''
    function : void f() { }
    function : void g() { f(); }''')
    return 'success' in o

# good pass-by-value function call
def test_01():
    o, e = run('''
    function : int f (int i) { return i; }
    function : void g () { f(32); }''')
    return 'success' in o

# good pass-by-value function call
def test_02():
    o, e = run('''
    function : void f(int & i) { }
    function : void g() { int j; f(j); }''')
    return 'success' in o

# good pass-by-reference function call, implicit conversion
def test_03():
    o, e = run('''
    function : void f(float i) { }
    function : void g() { int i; f(i); }''')
    return 'success' in o

# wrong number of arguments
def test_04():
    o, e = run('''
    function : void f() { }
    function : void g() { f(42); }
    ''')
    return 'Number of arguments (1) differs from number of parameters (0).' in o

# pass by value + unassignable
def test_05():
    o, e = run('''
    function : void func(int i) { }
    function : void main() {
      float fl;
      func(fl);
    }''')
    return 'Argument of type float not assignable to value parameter i, of type int.' in o

# pass by reference + not equivalent
def test_06():
    o, e = run('''
    function : void func(float & fl) {  }
    function : void main() {
      int i;
      func(i);
    }''')
    return 'Argument of type int not equivalent to reference parameter fl, of type float.' in o

# pass by reference + using equivalent except for typedef
def test_07():
    o, e = run('''
    typedef bool B;
    function : void func(B & bigb) { }
    function : void main() {
      bool littleb;
      func(littleb);
    }''')
    return 'success' in o

# pass by reference + using equivalent except for typedef
def test_08():
    o, e = run('''
    typedef bool B;
    function : void func(bool & littleb) { }
    function : void main() {
      B bigb;
      func(bigb);
    }''')
    return 'success' in o
    
# pass by reference + not a modifiable lval
def test_09():
    o, e = run('''
    function : void func(int* & i) { }
    function : void main() {
      int i;
      func(&i);
    }''')
    return 'Argument passed to reference parameter i (type int*) is not a modifiable L-value.' in o

# check for multiple errors in the right order
def test_10():
    o, e = run('''
    function : void func(int & i1, int i2) { }
    function : void main() {
      func(52, 10.5);
    }''')
    return '''Error, "(stdin)": 
  Argument passed to reference parameter i1 (type int) is not a modifiable L-value.
Error, "(stdin)": 
  Argument of type float not assignable to value parameter i2, of type int.
Compile: failure.
''' in o


# member func, no args, correct
def test_11():
    o, e = run('''
    structdef S {
      function : void sf() { }
    };
    function : void main() {
      S s;
      s.sf();
    }''')
    return 'success' in o


# member func, 1 arg, correct
def test_12():
    o, e = run('''
    structdef S {
      function : void sf(int * i) { }
    };
    function : void m() {
      S s;
      int i;
      s.sf(&i);
    }''')
    return 'success' in o
      

# member func, an arg, correct
def test_13():
    o, e = run('''
    structdef S {
      function : void sf(S * anothersf) { }
    };
    function : void m() {
      S s1, s2;
      s1.sf(&s2);
    }
    ''')
    return 'success' in o

# member func, using undefined type
def test_13a():
    o, e = run('''
    structdef S {
      function : void sf(T * anothersf) { }
    };
    structdef T { };
    function : void m() {
      S s1
      T s2;
      s1.sf(&s2);
    }
    ''')
    return "undeclared identifier 'T'." in o


# member func, misc problem
def test_14():
    o,e = run('''
    structdef S {
      function : void sf(float & f) { }
    };
    function : void m() {
      S s;
      s.sf(3.1415);
    }''')
    return 'Argument passed to reference parameter f (type float) is not a modifiable L-value.' in o

## tests for bad function definitions

# function name already taken
def test_15():
    o,e = run('''
    float f;
    function : void f() { }
    ''')
    return "redeclared identifier 'f'." in o

# invalid return type
def test_16():
    o,e = run(''' function : A f() { }''')
    return "undeclared identifier 'A'." in o

# invalid parameter type
def test_17():
    o,e = run(''' function : void f(B b) { } ''')
    return "undeclared identifier 'B'." in o


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
