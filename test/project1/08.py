from test import run

### const variable initialization

# good const variable init with literal
def test_01():
    o,e = run('''const float pi = 3.1415;''')
    return 'success' in o

# good const variable init with folded const expression
def test_02():
    o,e = run('''const int i = 3 + 4;''')
    return 'success' in o and '<IntValue>7</IntValue>' in e

def test_03():
    o,e = run('''const int i = 0 - 4;''')
    return 'success' in o and '<IntValue>-4</IntValue>' in e

def test_04():
    o,e = run('''const bool b = true || false;''')
    return 'success' in o and '<BooleanValue>true</BooleanValue>' in e

def test_05():
    o,e = run('const int i = (int) (3.6 + 3.7);')
    return 'success' in o and '<IntValue>7</IntValue>' in e

# good const variable init with another const variable
def test_06():
    o,e = run('const float f = 3.1415; const int i = (int)f;')
    return 'success' in o and '<IntValue>3</IntValue>' in e

# good const variable init with sth not equal type but assignable
def test_07():
    o,e = run('const int i = 4; const float f = i + 1;')
    return 'success' in o and '<FloatValue>5.0' in e

# trying to init const variable w/o initializer
def test_08():
    o,e = run('const bool b;')
    return 'syntax error near ";".' in o

# trying to init const variable with sth not known at compile time
def test_09():
    o,e = run('bool b1; const bool b2 = b1;')
    return "Initialization value of constant named b2 not known at compile time." in o

# trying to init const variable with sth not assignable
def test_10():
    o, e = run('const bool b = 7;')
    return "Initialization value of type int not assignable to constant/variable of type bool." in o

# trying to init const variable with arithmetic error
def test_11():
    o, e = run('const int i = 3 / 0;')
    return "Arithmetic exception occurred during constant folding." in o

# init with const of typedef type
def test_12():
    o, e = run('''
    typedef int I;
    const I i = (I) 17.2;
    ''')
    return 'success' in o
    
### non-const variable initialization

# equivalent type
def test_13():
    o,e = run('int i; int j = i;')
    return 'success' in o

# typedef-equiv type
def test_14():
    o,e = run('''
    typedef int I;
    typedef int J;
    I i;
    J j = i;
    ''')
    return 'success' in o

# assignable type
def test_15():
    o,e = run('''
    int i;
    float f = i;
    ''')
    return 'success' in o

# wrong type
def test_16():
    o,e = run('''
    float f;
    bool b = f;
    ''')
    return "Initialization value of type float not assignable to constant/variable of type bool." in o

# wrong type + typedef
def test_17():
    o,e = run('''
    typedef float F;
    F f;
    typedef bool B;
    B b = f;
    ''')
    return "Initialization value of type F not assignable to constant/variable of type B." in o

def test_18():
    o,e = run('int[3] ia = 42;')
    return 'Initialization value of type int not assignable to constant/variable of type int[3].' in o

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
