from test import run

# good : size is int literal
def test_01():
    o,e = run('''float[17] ia;''')
    return 'success' in o

# good : size is const int variable
def test_02():
    o,e = run('const int i = 2; bool[i] ba;')
    return 'success' in o

# good : size is const cast to int
def test_03():
    o,e = run('const bool b = true; bool[(int)b] ba;')
    return 'success' in o

# good : size is cast to (typedef to) int
def test_04():
    o,e = run('''
    typedef int I;
    typedef int * PI;
    const float f = 3.1415;
    PI[(I)f] pia;
    ''')
    return 'success' in o

# good : size is typedef to int
def test_05():
    o,e = run('''
    typedef int I;
    const I i = 42;
    float[i] fa;
    ''')
    return 'success' in o

# good : size is expr evaluating to int
def test_06():
    o,e = run('''
    const int i = 4;
    bool[i*i-i] ba;
    ''')
    return 'success' in o

# bad : literal not equiv to int
def test_07():
    o,e = run('''
    int[3.1415] ia;
    ''')
    return 'Index expression type (float) in array declaration not equivalent to int.' in o


# bad : const var not equiv to int
def test_08():
    o,e = run('''
    const float pi = 3.1415;
    int[pi] ia;
    ''')
    return 'Index expression type (float) in array declaration not equivalent to int.' in o

# bad : typedef is not equiv to int
def test_09():
    o,e = run('''
    typedef bool B;
    const B b= true;
    int[b] ia;
    ''')
    return 'Index expression type (B) in array declaration not equivalent to int.' in o

# bad : size not known at compile time
def test_10():
    o,e = run('''
    int i;
    bool[i] ba;
    ''')
    return 'Value of index expression not known at compile time.' in o

# bad : size is 0 or less
def test_11():
    o,e = run(''' bool[0] ba;''')
    return 'Index expression value (0) in array declaration must be > 0.' in o
    
def test_12():
    o,e = run('''
    const int a  = 5;
    float[a-a-a] fa;
    ''')
    return 'Index expression value (-5) in array declaration must be > 0.' in o

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
