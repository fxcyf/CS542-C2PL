package elements;

import java.io.Serializable;
import java.util.*;

public class Transaction implements Serializable {

    private int sid;

    private int tid;

    private List<Operation> ops;

    private List<Lock> locks;

    private Map<String, Integer> stored_values;

    private Map<String, Integer> pre_commit;

    private static final long serialVersionUID = 22L;

    public Transaction(int sid, int tid) {
        this.sid = sid;
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

    public int getSID() {
        return sid;
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
        Lock newLock = new Lock(sid, tid, op.getArg(), op.getType());
        for (Lock lock : locks) {
            if (lock.getItem().equals(newLock.getItem())) {
                lock.updateType();
                return;
            }
        }
        locks.add(newLock);
        return;
    }

    public boolean hasLock(Operation op) {

        // customPrint.printout("Transaction locks: " + locks);
        Lock desiredLock = new Lock(sid, tid, op.getArg(), op.getType());
        // customPrint.printout("Desired lock: " + desiredLock);
        for (Lock lock : locks) {
            if (lock.getItem().equals(desiredLock.getItem())) {
                // customPrint.printout("Found same item lock: " + desiredLock);
                if (lock.getType() >= desiredLock.getType()) {
                    return true;
                } else {
                    return false;
                    // customPrint.printout("Need higher lock: " + desiredLock);
                }
            }
        }
        return false;
    }

    public List<Lock> getLocks() {
        return locks;
    }

    @Override
    public String toString() {
        return String.format("Transaction %d", tid);
    }
}
