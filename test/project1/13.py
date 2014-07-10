from test import run

# good struct definition
def test_01():
    o,e = run(''' structdef S { int i; float f; bool b; };''')
    return 'success' in o

# struct definition using the same ID twice (bad)
def test_02():
    o,e = run(''' structdef S {int i; float i;};''')
    return "Field i declared second time in struct." in o

def test_03():
    o,e = run(''' structdef S {int i; int i;};''')
    return "Field i declared second time in struct." in o

def test_04():
    o,e = run(''' structdef S {int i; function : void i() { } };''')
    return "Field i declared second time in struct." in o

# recursive struct def (bad)
def test_05():
    o,e = run(''' structdef S { S next; }; ''')
    return "Size of field next cannot be determined at compile time." in o

# structdef with pointer to self (okay)
def test_06():
    o,e = run(''' structdef S { S* next; }; ''')
    return 'success' in o

def test_07():
    o,e = run(''' structdef S { S** next; }; ''')
    return 'success' in o

# what should this be?
## def test_08():
##     o,e = run(''' structdef S { typedef S* SP; SP[5] asp; }; ''')
##     print o


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
