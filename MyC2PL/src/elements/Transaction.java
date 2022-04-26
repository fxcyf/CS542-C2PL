package elements;

import elements.Operation;
import java.util.*;

public class Transaction {
    private int tid;

    private List<Operation> ops;

    private List<Lock> locks;

    private Map<String, Integer> read_values;

    public Transaction(int tid) {
        this.tid = tid;
        ops = new ArrayList<>();
        locks = new ArrayList<>();
        read_values = new HashMap<>();
    }

    public void reset() {
        tid = -1;
        locks = new ArrayList<>();
        read_values = new HashMap<>();
    }

    public int getTid() {
        return tid;
    }

    public void addOperation(Operation op) {
        ops.add(op);
    }

    public void addOperations(List<Operation> ops) {
        this.ops.addAll(ops);
    }

    public List<Operation> getOperations() {
        return ops;
    }

    public Map<String, Integer> getReadValues() {
        return read_values;
    }

    public void receiveLock(Operation op) {
        locks.add(new Lock(tid, op.getArg(), op.getType()));
    }
}
