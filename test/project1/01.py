# testing stuff mentioned in Check #1

# todo:

# need to find a way to test type of an expression (use incorrect
# return value)

# need to make sure we do checking for left argument first

# make sure what we're returning is an r value

# unary negation folding

from test import run

# test plus / minus / times / divide
def test_1() :
    prog = '''
    int a, b;
    function : int f () { return a + b; }
    '''
    return 'success' in run(prog)[0]

def test_2() :
    prog = '''
    int a, b;
    function : int f () { return a - b; }
    '''
    return 'success' in run(prog)[0]

def test_3() :
    prog = '''
    int a, b;
    function : int f () { return a * b; }
    '''
    return 'success' in run(prog)[0]

def test_4() :
    prog = '''
    int a, b;
    function : int f () { return a / b; }
    '''
    return 'success' in run(prog)[0]

def test_5():
    o, e = run('const int a = 5 + 6;')
    return 'success' in o and '11' in e

def test_6():
    o, e = run ('const int a = 5 - 6;')
    return 'success' in o and '<IntValue>-1</IntValue>' in e

def test_7():
    o, e = run('const int a = 12 / 5;')
    return 'success' in o and '<IntValue>2</IntValue>' in e

def test_8():
    o, e = run ('const int a = 10 * -2;')
    return 'success' in o and '<IntValue>-20</IntValue>' in e

def test_8a():
    o,e = run('const int a = 10 * +2;')
    return 'success' in o and '<IntValue>20</IntValue>' in e

def test_9():
    o, e = run('const float f = 10.1 + 1;')
    return 'success' in o and '<FloatValue>11.1</FloatValue>' in e

def test_10():
    o, e = run('function : void f() { 10 + true; }')
    return 'Incompatible type bool to binary operator +, numeric expected' in o

def test_11():
    o, e = run('function : void f() { false * 11.32; }')
    return 'Incompatible type bool to binary operator *, numeric expected' in o

def test_12():
    o, e = run('typedef int I; I i; function : void f() { i + i; }')
    return 'success' in o

def test_13():
    o, e = run('typedef float F; F f1; typedef int I; I i1; function : int f() { return f1 + i1; }')
    return "Type of return expression (float), not assignment compatible with function's return type (int)." in o


# testing mod

def test_14():
    o, e = run('function : int f() { int i, j;  return i % j; }')
    return 'success' in o

def test_15():
    o, e = run('function : int f() { float f; int i; return f % i; }')
    return  'Incompatible type float to binary operator %, equivalent to int expected.' in o

def test_16():
    o, e = run('function : int f() { float f; int i; return i % f; }')
    return  'Incompatible type float to binary operator %, equivalent to int expected.' in o


# testing inequality operators

def test_17():
    o, e = run('const bool a = 1 < 2;')
    return 'success' in o and '<BooleanValue>true</BooleanValue>' in e

def test_18():
    o, e = run('const bool a = 1.5 <= 2;');
    return 'success' in o and '<BooleanValue>true</BooleanValue>' in e

def test_19():
    o, e = run('const bool a = 3 > 3.0;');
    return 'success' in o and '<BooleanValue>false</BooleanValue>' in e

def test_20():
    o, e = run('const bool a = 3.7 >= 3.9;');
    return 'success' in o and '<BooleanValue>false</BooleanValue>' in e

def test_21():
    o, e = run('const bool b = true > 3;');
    return 'Incompatible type bool to binary operator >, numeric expected.' in o

def test_22():
    o, e = run('structdef S { int i; }; S s; const bool b = 2.0 > s;');
    return 'Incompatible type S to binary operator >, numeric expected.' in o

# check that typedef names are printed out
def test_22a():
    o,e = run('''
    typedef bool B, C, D;
    B b;
    C c;
    D d = b < c;
    ''')
    return 'Incompatible type B to binary operator <, numeric expected.' in o


def test_23():
    o, e = run('''
    typedef int I;
    typedef I II;
    typedef float F;
    II ii;
    F f;
    bool b = ii >= f;
    ''')
    return 'success' in o

# testing equality operators

def test_24():
    o, e = run('bool b = 1==1;')
    return 'success' in o

def test_25():
    o, e = run('bool b = false == 2;')
    return 'Incompatible types to operator: bool == int;' in o

def test_26():
    o, e = run('bool b = 3.5 == true;')
    return 'Incompatible types to operator: float == bool;' in o

def test_27():
    o, e = run('bool b = true == false;')
    return 'success' in o

def test_28():
    o, e = run('''
    typedef bool B;
    typedef B BB;
    BB bb1, bb2;
    B b = bb2 != 14;
    ''')
    return 'Incompatible types to operator: BB != int;' in o
    
def test_29():
    o, e = run('''
    typedef bool B;
    typedef B BB;
    BB bb1, bb2;
    B b = bb1 != bb2;
    ''')
    return 'success' in o
    

# testing and, or, not

def test_30():
    o, e = run('const bool b = true || false;')
    return 'success' in o and '<BooleanValue>true</BooleanValue>' in e

def test_31():
    o, e = run('''
    typedef bool B;
    B a, b;
    bool c = a && b;''')
    return 'success' in o

def test_32():
    o, e = run(''' const bool b = ! true;''')
    return 'success' in o and '<BooleanValue>false</BooleanValue>' in e

def test_33():
    o, e = run (' const bool b = ! false;')
    return 'success' in o and '<BooleanValue>true</BooleanValue>' in e

def test_34():
    o, e = run('bool b = 5 || true;')
    return 'Incompatible type int to binary operator ||, equivalent to bool expected.' in o

def test_35():
    o, e = run('bool b = true || 5;')
    return 'Incompatible type int to binary operator ||, equivalent to bool expected.' in o

def test_36():
    o, e = run('bool b = false && 7.0;')
    return 'Incompatible type float to binary operator &&, equivalent to bool expected.' in o

def test_37():
    o, e = run('bool b = ! 15.2;')
    return 'Incompatible type float to unary operator !, equivalent to bool expected.' in o

# testing ampersand, caret, and bar

def test_38():
    o, e = run('int i = 5 & 6;')
    return 'success' in o

def test_39():
    o, e = run('int i = 5 ^ 6;')
    return 'success' in o

def test_40():
    o, e = run('int i = 5 | 6;')
    return 'success' in o

def test_41():
    o, e = run('''
    typedef int I;
    typedef I II;
    typedef II III;
    III a, b;
    II ii = a | b;
    ''')
    return 'success' in o

def test_42():
    o, e = run(' int i = 5 | true;')
    return 'Incompatible type bool to binary operator |' in o

def test_43():
    o, e = run('''
    typedef bool B;
    B b;
    int i = 5 ^ b;''')
    return 'Incompatible type B to binary operator ^' in o

def test_44():
    o, e = run('''
    typedef int B;
    B b;
    int i = 5 ^ b;''')
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
