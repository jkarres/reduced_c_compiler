from test import run

# int literal
def test_01():
    o,e = run(''' function : void f() { exit(1); }''')
    return 'success' in o

# int variable
def test_02():
    o,e = run(''' int i; function : void f() { exit(i);} ''')
    return 'success' in o

# const int variable
def test_03():
    o,e = run ('const int i = 42; function : void f() { exit(i); }')
    return 'success' in o

# typedefed as int
def test_04():
    o,e = run('''
    typedef int I;
    typedef I II;
    II ii;
    function : void f() { exit(ii); }
    ''')
    return 'success' in o

# expr yielding sth typpedefed as int
def test_05():
    o, e = run('''
    typedef int I;
    function : I gi() { return 27; }
    function : void f() { exit(gi()); }
    ''')
    return 'success' in o

# empty
def test_06():
    o,e = run('''function : void f () { exit(f()); }''')
    return 'Exit expression (type void) is not assignable to int.' in o

# float
def test_07():
    o,e = run('function : void f() { exit(3.1415); }')
    return 'Exit expression (type float) is not assignable to int.' in o

# typedefed as float
def test_08():
    o,e = run('''
    typedef float F;
    function : void f() { F fl;  exit(fl); }''')
    return 'Exit expression (type F) is not assignable to int.' in o

# cast to typedef equiv to int
def test_09():
    o,e = run('''
    typedef float F;
    typedef int I;
    function : void f() { F fl;  exit((I)fl); }''')
    return 'success' in o

# array name
def test_10():
    o, e = run('''
    int[24] ia;
    function : void f() {
      exit(ia);
    }''')
    return 'Exit expression (type int[24]) is not assignable to int.' in o


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
