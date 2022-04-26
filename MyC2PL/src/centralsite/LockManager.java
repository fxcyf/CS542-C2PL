package centralsite;

import java.util.*;

import javax.imageio.plugins.tiff.ExifGPSTagSet;

import elements.Lock;
import elements.Operation;
import elements.Transaction;

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

    public Lock findTransLock(String opArg, int tid) {
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

    public void storeLock(Operation op) {
        String opArg = op.getArg();
        int opType = op.getType();
        Lock newLock = new Lock(op.getTid(), opArg, opType);

        if (lockTable.containsKey(opArg)) {
            lockTable.get(opArg).add(newLock);
        } else {
            List<Lock> lockList = new ArrayList<>();
            lockList.add(newLock);
            lockTable.put(opArg, lockList);
        }
    }

    public Boolean lockCompatible(Operation op, List<Lock> existing_locks) {

        switch (op.getType()) {
            case Operation.readType: {
                for (Lock oldLock : existing_locks) {
                    if (oldLock.getType() > Operation.readType) {
                        return false;
                    }
                }
                return true;
            }
            case Operation.writeType: {
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

        List<Lock> exsiting_item_locks = findLock(op.getArg());

        if (exsiting_item_locks.size() == 0) {
            storeLock(op);
            return true;
        } else if (lockCompatible(op, exsiting_item_locks)) {
            Lock oldLock = findTransLock(op.getArg(), op.getTid());
            if (oldLock != null) {
                oldLock.updateType();
            } else {
                storeLock(op);
            }
            return true;
        } else {
            addToQueue(op);
            return false;
        }
    }

    public void releaseLock(Transaction trans) {
        List<Lock> transLocks = trans.getLocks();

        for (Lock transLock : transLocks) {
            String item = transLock.getItem();
            List<Lock> existingItemLocks = lockTable.get(item);
            for (Lock lock : existingItemLocks) {
                if (lock.getTid() == trans.getTID()) {
                    existingItemLocks.remove(lock);
                }
            }

        }

    }

}
