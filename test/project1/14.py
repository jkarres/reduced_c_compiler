from test import run

# good struct member access
def test_01():
    o,e = run('''
    structdef S { int i; float f; bool b;};
    function : void f() {
      S s;
      s.i = 15;
      s.f = 3.1415;
      s.b = false;
    }''')
    return 'success' in o

# rhs of . not a struct
def test_02():
    o,e = run('''
    int i, j;
    function : void f() {
      i.j = 5;
    }''')
    return 'Type of expression referenced by "." (int) is not a struct.' in o

# lhs of . not a member
def test_03():
    o,e = run('''
    structdef S { int i; };
    function : void f() {
      S s;
      s.j = 5;
    }''')
    return 'Referenced field j not found in type S.' in o

# good use of this
def test_04():
    o,e = run('''
    structdef S {
      int i, j;
      function : int total() {
        return this.i + this.j;
      }
    };''')
    return 'success' in o

# trying to use this w/ non-member
def test_05():
    o,e = run('''
    int globali;
    structdef S {
      float fl;
      function : int fn() {
        return this.i;
      }
    };''')
    return 'Referenced field i not found in current struct.' in o and 'Return required in function, none found.' not in o
    

# trying to access struct scope without this : make sure this is the
# right error message (doesn't seem to be any other...)
def test_06():
    o,e = run('''
    structdef S {
      int imem;
      function : int fn() {
        return imem;
      }
    };''')
    return "undeclared identifier 'imem'." in o

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
