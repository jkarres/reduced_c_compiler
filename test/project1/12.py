from test import run

# test break in for loop
def test_01():
    o,e = run('''
    function : void f() {
      int i;
      for (i = 0; i != 10; ++i) {
        break;
      }
    }''')
    return 'success' in o

# test continue in for loop
def test_02():
    o,e = run('''
    function : void f() {
      int i;
      for (i = 0; i != 10; ++i) {
        continue;
      }
    }''')
    return 'success' in o
    
# test in for/if
def test_03():
    o,e = run('''
    function : void f() {
      int i;
      for (i = 0; i != 10; ++i) {
        if (i % 2 == 0) {
          continue;
        }
      }
    }''')
    return 'success' in o


# test break deep in for loop, but not deepest
def test_04():
    o,e = run('''
    function : void f() {
      int i, k = 0;
      for (i = 0; i != 10; ++i) {
        int j;
        for (j = 9; j != 0; j--) {
          k++;
        }
        if (i % 2 == 0) {
          break;
        }
      }
    }''')
    return 'success' in o

# test break just in function
def test_05():
    o,e = run('''
    function : void f() {
      break;
    }''')
    return 'Break does not occur in a loop.' in o

# test continue just in function
def test_06():
    o,e = run('''
    function : void f() {
      continue;
    }''')
    return 'Continue does not occur in a loop.' in o

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
