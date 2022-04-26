package centralsite;

import java.util.*;

import javax.imageio.plugins.tiff.ExifGPSTagSet;

import elements.Lock;
import elements.Operation;

public class LockManager {
    Map<String, List<Lock>> lockTable;
    Map<String, Queue<Operation>> queueTable;

    public LockManager() {
        lockTable = new HashMap<>();
        queueTable = new HashMap<>();
    }

    public List<Lock> findLock(String opArg) {
        if (lockTable.containsKey(opArg)) {
            return lockTable.get(opArg);
        } else
            return new ArrayList<Lock>();
    }

    public Lock findTLock(String opArg, int tid) {
        if (lockTable.containsKey(opArg)) {
            List<Lock> itemLocks = lockTable.get(opArg);
            for (Lock oldlock : itemLocks) {
                if (oldlock.getTid() == tid) {
                    return oldlock;
                }
            }
        }
        return null;
    }

    public void grantLock(Operation op) {
        String opArg = op.getArg();
        String opType = op.getType();
        Lock newLock = new Lock(op.getTid(), opArg, opType);

        if (lockTable.containsKey(opArg)) {
            lockTable.get(opArg).add(newLock);
        } else {
            List<Lock> lockList = new ArrayList<>();
            lockList.add(newLock);
            lockTable.put(opArg, lockList);
        }
    }

    public void updateLock(Operation op) {

    }

    public Boolean lockExisting(Operation op, List<Lock> existing_locks) {
        for (Lock oldLock : existing_locks) {
            if (oldLock.getTid() == op.getTid() && oldLock.getType() == op.getType()) {
                return true;
            } else if (oldLock.getTid() == op.getTid() && oldLock.getType() == "write" && op.getType() == "read") {
                return true;
            }
        }
        return false;
    }

    public Boolean lockCompatible(Operation op, List<Lock> existing_locks) {

        switch (op.getType()) {
            case "read": {
                for (Lock oldLock : existing_locks) {
                    if (oldLock.getType() == "write") {
                        return false;
                    }
                }
                return true;
            }
            case "write": {
                for (Lock oldLock : existing_locks) {
                    if (oldLock.getTid() != op.getTid()) {
                        return false;
                    }
                }
                return true;
            }
            default:
                return false;
        }
    }

    public void addToQueue(Operation op) {
        if (queueTable.containsKey(op.getArg())) {
            queueTable.get(op.getArg()).offer(op);
        } else {
            Queue<Operation> queue = new LinkedList<>();
            queue.offer(op);
            queueTable.put(op.getArg(), queue);
        }
    }

    public Boolean requestLock(Operation op) {

        List<Lock> exsiting_locks = findLock(op.getArg());

        if (exsiting_locks.size() == 0) {
            grantLock(op);
            return true;
        } else if (lockExisting(op, exsiting_locks)) {
            return true;
        } else if (lockCompatible(op, exsiting_locks)) {
            Lock oldLock = findTLock(op.getArg(), op.getTid());
            if (oldLock != null) {
                oldLock.updateType();
            } else {
                grantLock(op);
            }
            return true;
        } else {
            addToQueue(op);
            return false;
        }
    }

}
