
from test import run

# using if +  bool literal
def test_01():
    o, e = run('''
    function : void f() {
      if (true) { }
    }
    ''')
    return 'success' in o


# using if + int literal
def test_02():
    o, e = run('''
    function : void f() {
      if (42) { }
    }
    ''')
    return 'success' in o

# using if + float literal
def test_03():
    o,e = run('''
    function : void f() {
      if (3.1415) { }
    }
    ''')
    return 'bool or int required for conditional test, float found.' in o

# using for + bool literal
def test_04():
    o, e = run('''
    function : void f() {
      int i, j;
      for (i = 0; false; ++i) {
        ++j;
      }
    }''')
    return 'success' in o

# using for + int variable
def test_05() :
    o, e = run('''
    function : void f() {
      int i, j;
      for ( i = 0; j; ++i) {
        j = i;
      }
    }''')
    return 'success' in o

# using for + float variable
def test_06():
    o, e = run('''
    function : void f() {
      float f = 45.0;
      int i;
      for (i = 0; f; ++i) {
        f = f - i;
      }
    }''')
    return 'bool or int required for conditional test, float found.' in o

# using for + nothing
def test_07():
    o, e = run('''
    function : void f() {
      int i;
      for ( ; ; ) { ++i; }
    }''')
    return 'success' in o

# using if + good typedef
def test_08() :
    o, e = run ('''
    typedef bool B;
    B b;
    function : void f() {
      if (b) { b = false; }
    }''')
    return 'success' in o

# using if + good typedef
def test_09():
    o, e = run('''
    typedef int I;
    I i;
    function : void f() {
      if (i) { ++i; }
    }''')
    return 'success' in o

# using if + ptr to bool
def test_11():
    o, e = run('''
    bool* b;
    function : void f() {
      if (b) { b++; }
    }''')
    return 'bool or int required for conditional test, bool* found.' in o

# using if + struct
def test_10():
    o, e = run('''
    structdef S { bool b; };
    S s;
    function : void f() {
      if (s) { }
    }
    ''')
    return 'bool or int required for conditional test, S found.' in o

# using if + bad typedef
def test_12():
    o, e = run('''
    typedef float F;
    F f;
    function : void g() {
      if (f) { f++; }
    }''')
    return 'bool or int required for conditional test, F found.' in o

# using for + good long typedef
def test_13():
    o, e = run('''
    typedef bool B;
    typedef B BB;
    typedef BB BBB;
    typedef BBB BBBB;
    function : void f() {
      BBBB bbbb;
      for ( ; bbbb; ) { }
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
