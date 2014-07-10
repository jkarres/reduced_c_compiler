from test import run

# test constant folding with constant variables
def test_01():
    o,e = run('''
    typedef int I;
    typedef I II;
    typedef int J;
    typedef J JJ;
    const II zero = 0;
    const JJ one = (J) 1;
    const int k = one + one + one + one;
    ''')
    return 'success' in o and '<IntValue>4</IntValue>' in e

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
