# testing stuff mentioned in check #2

from test import run

def test_01():
    o, e = run('int i; function : int f() { return ++i; }')
    return 'success' in o

def test_02():
    o, e = run('int i; function : int f() { return i++; }')
    return 'success' in o

def test_03():
    o, e = run('int i; function : int f() { return --i; }')
    return 'success' in o

def test_04():
    o, e = run('int i; function : int f() { return i--; }')
    return 'success' in o

def test_05():
    o, e = run('float i; function : float f() { return ++i; }')
    return 'success' in o

def test_06():
    o, e = run('float i; function : float f() { return i++; }')
    return 'success' in o

def test_07():
    o, e = run('float i; function : float f() { return --i; }')
    return 'success' in o

def test_08():
    o, e = run('float i; function : float f() { return i--; }')
    return 'success' in o

def test_09():
    o, e = run('float i; function : int f() { return ++i; }')
    return "Type of return expression (float), not assignment compatible with function's return type (int)." in o

def test_10():
    o, e = run('int i; int j = ++i;')
    return 'success' in o

def test_11():
    o, e = run('int i = ++3;')
    return 'Operand to ++ is not a modifiable L-value.' in o

def test_12():
    o, e = run ('int i = ++false;')
    return 'Incompatible type bool to operator ++, equivalent to int, float, or pointer expected.' in o

def test_13():
    o, e = run('const int j = 5; int k = ++j;')
    return 'Operand to ++ is not a modifiable L-value.' in o

def test_14():
    o, e = run('typedef bool B; B b = true; int i = ++b;')
    return 'Incompatible type B to operator ++, equivalent to int, float, or pointer expected.' in o

def test_15():
    o, e = run('''
    typedef int I;
    typedef I II;
    II ii;
    float f = ++ii;
    ''')
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
