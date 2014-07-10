import java.util.*;

public class StructScope extends Scope {

    private IncompleteStructType type;
    private String name;
    private Map<String, Integer> offsetMap = new HashMap<String, Integer>();
    private int currentOffset = 0;

    public StructScope(String s) {
        this.name = s;
        this.type = new IncompleteStructType(name, this);
        super.InsertLocal(this.type);
    }

    public String getName() {
        return name;
    }

    public IncompleteStructType getType() {
        return type;
    }

    // you can't get a member just like this.  you need to use
    // accessMember, except for the struct name
    public STO accessLocal(String id) {
        if (id.equals(name)) {
            return super.accessLocal(id);
        } else {
            return null;
        }
    }

    public STO accessMember(String name) {
        return super.accessLocal(name);
    }

    public void InsertLocal(STO sto) {
        super.InsertLocal(sto);
        if (sto instanceof Expr) {
            if (((Expr)sto).getType() instanceof Type && !(sto instanceof UnboundFunction)) {
                offsetMap.put(sto.getName(), currentOffset);
                currentOffset += ((Type)((Expr)sto).getType()).size();
            }
        }
    }

    public int getOffset(String name) {
        return offsetMap.get(name);
    }

    public int size() {
        //System.out.println("Entering StructScope.size(), name is "  + name);
        int retval = 0;
        for (STO sto : locals) {
            if (sto instanceof Expr) {
                if (((Expr)sto).getType() instanceof Type && !(sto instanceof UnboundFunction)) {
                    int s = ((Type)(((Expr)sto).getType())).size();
                    retval +=s;
                    //System.out.println(sto.getName() + ": " + s);
                }
            }
        }
        return retval;
    }

}
