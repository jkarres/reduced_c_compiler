from subprocess import PIPE, Popen
from os import environ

def run(code):
    if environ['USER'] == 'jkarres':
        pop = Popen('~/cse131/code/RC -supersecretdebug',
                    env={'CLASSPATH' :
                         '.:/Users/jkarres/cse131/tools/java-cut-v11a.jar:/Users/jkarres/cse131/tools/JFlex.jar:/Users/jkarres/cse131/code'},
                    shell=True, stdin=PIPE, stdout=PIPE, stderr=PIPE)
        pop.stdin.write(code)
        pop.stdin.close()
        return (pop.stdout.read(), pop.stderr.read())
    elif environ['USER'] == 'cs131sag':
        pop = Popen('/home/solaris/ieng9/cs131s/cs131sag/cse131/code/RC -supersecretdebug',
                    env={'CLASSPATH' :
                         '.:/home/solaris/ieng9/cs131s/public/Tools/java_cup-v11a.jar:/home/solaris/ieng9/cs131s/public/Tools/JFlex/lib/JFlex.jar:/home/solaris/ieng9/cs131s/cs131sag/cse131/code'},
                    shell=True, stdin=PIPE, stdout=PIPE, stderr=PIPE)
        pop.stdin.write(code)
        pop.stdin.close()
        return (pop.stdout.read(), pop.stderr.read())
        
