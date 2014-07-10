from test import run

# good new
def test_01():
    o,e = run('''
    function : void f() {
      int * pi;
      new pi;
    }''')
    return 'success' in o

# good delete
def test_02():
    o,e = run('''
    function : void f() {
      int * pi;
      new pi;
      delete pi;
    }''')
    return 'success' in o

# new on sth that isn't a modifiable lval
def test_03():
    o,e = run('''
    structdef S { int k; } ;
    S s;
    function : S * gets () {
      return &s;
    }
    function : void f() {
      new gets();
    }''')
    return 'Operand to "new" is not a modifiable L-value.' in o
      
# delete on sth that isn't a modifiable lval
def test_04():
    o,e = run('''
    structdef S { int k; } ;
    S s;
    function : S * gets () {
      return &s;
    }
    function : void f() {
      delete gets();
    }''')
    return 'Operand to "delete" is not a modifiable L-value.' in o

# new on sth that isn't a pointer
def test_05():
    o,e = run('''
    function : void f() {
      float[5] fa;
      new fa;
    }''')
    return "Type of new's operand must be of pointer type, float[5] found."

# delete on sth that isn't a pointer
def test_06():
    o,e = run('''
    structdef S { int i; float f; };
    function : void f() {
      S s;
      delete s;
    }''')
    return "Type of delete's operand must be of pointer type, S found." in o

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
