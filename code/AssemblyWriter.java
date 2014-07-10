import java.io.*;

class AssemblyWriter {
    private Section currentSection = null;

    private Writer writer;

    public AssemblyWriter(boolean wait) {
        if (wait) {
            writer = new StringWriter();
        } else {
            try {
                writer = new BufferedWriter(new FileWriter("rc.s"));
            } catch (IOException e) {
                System.out.println("An exception was thrown while trying " +
                                   "to open rc.s");
                System.exit(1);
            }
        }
    }

    public void dump(AssemblyWriter aw) {
        if (writer instanceof StringWriter) {
            aw.write( ((StringWriter)writer).getBuffer().toString());
        } else {
            assert false;
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) { }
    }

    public void write(String s) {
        try {
            writer.write(s + "\n");
        } catch (IOException e) {
            System.out.println("Error while trying to write to rc.s");
            System.exit(1);
        }
    }

    public void writeComment(String s) {
        try {
            writer.write("\t!\t" + s + "\n");
        } catch (IOException e) {
            System.out.println("Error while trying to write to rc.s");
            System.exit(1);
        }
    }

    public void writeGlobal(String name, int size, int alignment, boolean isStatic) {
        write("\t.section\t\".bss\"");
        write("\t.align\t" + alignment);
        if (!isStatic) {
            write("\t.global\t" + name);
        }
        write(name + ":");
        write("\t.skip\t" + size);
    }

    public void loadLocal(int offset, Register target) {
        if (-offset >= -4096 && -offset <= 4095) {
            if (offset > 0)
                write("\t" + "ld\t" + "[" + Register.FP + " - " + offset + "], " + target);
            else
                write("\t" + "ld\t" + "[" + Register.FP + " + " + -offset + "], " + target);
        } else {
            write("\t" + "set\t" + -offset + ", " + Register.G5);
            write("\t" + "ld\t" + "[" + Register.FP + " + " + Register.G5 + "], " + target);
        }
    }

    public void storeLocal(Register source, int offset) {
        if (-offset >= -4096 && -offset <= 4095) {
            if (offset > 0)
                write("\t" + "st\t" + source + ", [" + Register.FP + " - " + offset + "]");
            else
                write("\t" + "st\t" + source + ", [" + Register.FP + " + " + -offset + "]");
        } else {
            write("\t" + "set\t" + -offset + ", " + Register.G5);
            write("\t" + "st\t" + source + ", [" + Register.FP + " + " + Register.G5 + "]");
        }
        
    }

    public void subFromFramePointer(int subtrahend, Register destination) {
        if (subtrahend >= -4096 && subtrahend <= 4095) {
            write("\t" + "sub\t" + Register.FP + ", " + subtrahend + ", " + destination);
        } else {
            write("\t" + "set\t" + subtrahend + ", " + Register.G5);
            write("\t" + "sub\t" + Register.FP + ", " + Register.G5 + ", " + destination);
        }
    }


}

enum Section {
    
    TEXT(".text"), DATA(".data"), RODATA(".rodata"), BSS(".bss");

    public String name;

    Section(String s) {
        name = s;
    }

}
