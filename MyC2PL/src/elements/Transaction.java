package elements;

import elements.Operation;
import java.util.*;

public class Transaction {
    private int tid;

    private List<Operation> ops;

    private List<Lock> locks;

    private Map<String, Integer> stored_values;

    private Map<String, Integer> pre_commit;

    public Transaction(int tid) {
        this.tid = tid;
        ops = new ArrayList<>();
        locks = new ArrayList<>();
        stored_values = new HashMap<>();
        pre_commit = new HashMap<>();
    }

    public void reset() {
        tid = -1;
        locks = new ArrayList<>();
        stored_values = new HashMap<>();
        pre_commit = new HashMap<>();
    }

    public int getTID() {
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

    public Map<String, Integer> getStoredValues() {
        return stored_values;
    }

    public Map<String, Integer> getPreCommit() {
        return pre_commit;
    }

    public void receiveOrUpdateLock(Operation op) {
        Lock newLock = new Lock(tid, op.getArg(), op.getType());
        for (Lock lock : locks) {
            if (lock.getItem() == newLock.getItem()) {
                lock.updateType();
                return;
            }
        }
        locks.add(newLock);
        return;
    }

    public boolean hasLock(Operation op) {

        Lock desiredLock = new Lock(tid, op.getArg(), op.getType());

        for (Lock lock : locks) {
            if (lock.getItem() == desiredLock.getItem() && lock.getType() >= desiredLock.getType()) {
                return true;
            }
        }
        return false;
    }

    public List<Lock> getLocks() {
        return locks;
    }
}
