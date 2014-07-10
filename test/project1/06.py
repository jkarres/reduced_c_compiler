from test import run

# todo

# test typedefs


# good plain return statement
def test_01():
    o, e = run('function : void f() { int a; return; }')
    return 'success' in o

# returning exact type (okay)
def test_02():
    o, e = run('''
    int a;
    function : int* f() { return &a; }
    ''')
    return 'success' in o

# returning assignable type (okay)
def test_03():
    o, e = run('function : float f() { return 42; }')
    return 'success' in o

# incorrect plain return (bad)
def test_04():
    o, e = run('function : bool f() { return; }')
    return 'Return in function requires a result expression, none found.' in o

# returning sth from void func (bad)
def test_05():
    o, e = run('function : void f() { return false; }')
    return "Type of return expression (bool), not assignment compatible with function's return type (void)." in o

# returning non-assignable (bad)
def test_06():
    o, e = run(' function : int f() { return 23.4; }')
    return "Type of return expression (float), not assignment compatible with function's return type (int)." in o

# missing return in void func (okay)
def test_07():
    o, e = run('function : void f() { int i; ++i; }')
    return 'success' in o

# embedded return in void func (okay)
def test_08():
    o, e = run('''
    function : void f() {
      int i;
      if (true) {return; }
    }''')
    return 'success' in o

# embedded return in void func, but with a value (bad)
def test_09():
    o, e = run('''
    function : void f() {
      int i;
      for ( ; ; ) {
        return i;
      }
      return;
    }
    ''')
    return "Type of return expression (int), not assignment compatible with function's return type (void)." in o

# missing return in non-void func (bad)
def test_10():
    o, e = run(''' function : bool f() { return; }''')
    return 'Return in function requires a result expression, none found.' in o

# non-void func with only embedded returns (bad)
def test_11():
    o, e = run('''
    function : int f() {
      if (true) {
        return 5;
      } else {
        return 6;
      }
    }''')
    return 'Return required in function, none found.' in o

# non-void func with embedded and non-embedded returns (okay)
def test_12():
    o, e = run('''
    function : int f() {
      if (true) {
        return 5;
      } else {
        return 6;
      }
    return 7;
    }''')
    return 'success' in o
    
# non-void func with embedded and non-embedded returns, with some
# returning incorrect types (bad)
def test_13():
    o, e = run('''
    function : int f() {
      if (true) {
        return 5.5;
      } else {
        return 6;
      }
    return 7;
    }''')
    return "Type of return expression (float), not assignment compatible with function's return type (int)." in o
    

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
