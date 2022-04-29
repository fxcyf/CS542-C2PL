package centralsite;

import java.util.*;

import elements.Lock;
import elements.Operation;
import elements.Transaction;
import utils.customPrint;

public class LockManager {
    private Map<String, List<Lock>> itemLockTable;
    private Map<String, Queue<Operation>> queueTable;
    private Map<Integer, List<Lock>> siteLockTable;
    private Map<Integer, List<Integer>> waitForGraph;
    private int deadlockCount;

    public LockManager() {
        itemLockTable = new HashMap<>();
        siteLockTable = new HashMap<>();
        queueTable = new HashMap<>();
        deadlockCount = 0;
    }

    public List<Lock> findLock(String opArg) {
        if (itemLockTable.containsKey(opArg)) {
            return itemLockTable.get(opArg);
        } else
            return new ArrayList<Lock>();
    }

    public Lock findTransLock(String opArg, int tid) {
        if (itemLockTable.containsKey(opArg)) {
            List<Lock> itemLocks = itemLockTable.get(opArg);
            for (Lock oldlock : itemLocks) {
                if (oldlock.getTID() == tid) {
                    return oldlock;
                }
            }
        }
        return null;
    }

    public Lock findSiteLocks(String opArg, Integer sid) {
        if (itemLockTable.containsKey(opArg)) {
            List<Lock> itemLocks = itemLockTable.get(opArg);
            for (Lock oldlock : itemLocks) {
                if (oldlock.getSID() == sid) {
                    return oldlock;
                }
            }
        }
        return null;
    }

    public void storeLock(Operation op) {
        String opArg = op.getArg();
        int opType = op.getType();
        Lock newLock = new Lock(op.getSID(), op.getTID(), opArg, opType);

        if (itemLockTable.containsKey(opArg)) {
            itemLockTable.get(opArg).add(newLock);
        } else {
            List<Lock> lockList = new ArrayList<>();
            lockList.add(newLock);
            itemLockTable.put(opArg, lockList);
        }
        if (siteLockTable.containsKey(op.getSID())) {
            siteLockTable.get(op.getSID()).add(newLock);
        } else {
            List<Lock> lockList = new ArrayList<>();
            lockList.add(newLock);
            siteLockTable.put(op.getSID(), lockList);
        }
    }

    public Boolean lockCompatible(Operation op, List<Lock> existing_locks) {

        switch (op.getType()) {
            case Operation.readType: {
                for (Lock oldLock : existing_locks) {
                    if (oldLock.getType() == Lock.writeType) {
                        return false;
                    }
                }
                return true;
            }
            case Operation.writeType: {
                for (Lock oldLock : existing_locks) {
                    if (oldLock.getTID() != op.getTID()) {
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
        String arg = op.getArg();
        if (queueTable.containsKey(arg)) {
            queueTable.get(arg).offer(op);
        } else {
            Queue<Operation> queue = new LinkedList<>();
            queue.offer(op);
            queueTable.put(arg, queue);
        }
    }

    public Boolean requestLock(Operation op) {
        customPrint.printout(op + "is requesting lock... ");

        List<Lock> exsiting_item_locks = findLock(op.getArg());

        if (exsiting_item_locks.size() == 0) {
            storeLock(op);
            customPrint.printout("success");
            // customPrint.printout("itemLockTable " + itemLockTable);
            return true;
        } else if (lockCompatible(op, exsiting_item_locks)) {
            Lock oldLock = findTransLock(op.getArg(), op.getTID());
            if (oldLock != null) {
                oldLock.updateType();
            } else {
                storeLock(op);
            }
            customPrint.printout("success");
            // customPrint.printout("itemLockTable " + itemLockTable);
            return true;
        } else {
            addToQueue(op);
            customPrint.printout("fail");
            // customPrint.printout("Queue table " + queueTable);
            return false;
        }
    }

    public List<Integer> releaseLock(Transaction trans) {
        customPrint.printout("T" + trans.getTID() + " is releasing locks");

        List<Lock> transLocks = trans.getLocks();
        List<Integer> unblockingSite = new ArrayList<>();
        for (Lock transLock : transLocks) {
            String item = transLock.getItem();
            List<Lock> existingItemLocks = itemLockTable.get(item);
            List<Lock> releasingLocks = new ArrayList<>();
            for (Lock lock : existingItemLocks) {
                if (lock.getTID() == transLock.getTID()) {
                    releasingLocks.add(lock);
                }
            }

            itemLockTable.get(item).removeAll(releasingLocks);
            siteLockTable.remove(trans.getSID());

            Queue<Operation> itemQueue = queueTable.getOrDefault(item, new LinkedList<>());
            while (!itemQueue.isEmpty()) {
                // customPrint.printout("Existing locks: " + itemLockTable.get(item));
                // customPrint.printout("Item queue: " + itemQueue);

                Operation peekOp = itemQueue.peek();
                if (lockCompatible(peekOp, itemLockTable.get(item))) {
                    // customPrint.printout("Queue peek compatible with existing locks");

                    if (itemLockTable.get(item).size() == 0) {
                        storeLock(peekOp);
                        // customPrint.printout("success");
                        // customPrint.printout("itemLockTable " + itemLockTable);
                    } else {
                        Lock oldLock = findTransLock(peekOp.getArg(), peekOp.getTID());
                        if (oldLock != null) {
                            oldLock.updateType();
                        } else {
                            storeLock(peekOp);
                        }
                        // customPrint.printout("success");
                        // customPrint.printout("itemLockTable " + itemLockTable);
                    }

                    unblockingSite.add(peekOp.getSID());
                    itemQueue.poll();
                    if (itemQueue.size() == 0) {
                        queueTable.remove(item);
                        break;
                    }
                } else {
                    // customPrint.printout("Queue peek not compatible with existing locks");
                    break;
                }
            }
        }
        customPrint.printout("Going to unblock site " + unblockingSite);

        return unblockingSite;
    }

    public List<Integer> releaseSiteLock(Integer siteID) {
        customPrint.printout("Site" + siteID + " is releasing locks");

        List<Integer> unblockingSite = new ArrayList<>();
        for (String item : queueTable.keySet()) {
            Queue<Operation> waitingOps = queueTable.getOrDefault(item, new LinkedList<>());
            List<Operation> releasingwaitingQueue = new ArrayList<>();

            for (Operation op : waitingOps) {
                if (op.getSID() == siteID) {
                    releasingwaitingQueue.add(op);
                }
            }
            waitingOps.removeAll(releasingwaitingQueue);

        }

        for (Lock siteLock : siteLockTable.get(siteID)) {
            String item = siteLock.getItem();
            List<Lock> existingItemLocks = itemLockTable.get(item);
            List<Lock> releasingLocks = new ArrayList<>();
            for (Lock lock : existingItemLocks) {
                if (lock.getSID() == siteID) {
                    releasingLocks.add(lock);
                }
            }

            itemLockTable.get(item).removeAll(releasingLocks);

            Queue<Operation> itemQueue = queueTable.getOrDefault(item, new LinkedList<>());
            while (!itemQueue.isEmpty()) {
                // customPrint.printout("Existing locks: " + itemLockTable.get(item));
                // customPrint.printout("Item queue: " + itemQueue);
                Operation peekOp = itemQueue.peek();
                if (lockCompatible(peekOp, itemLockTable.get(item))) {
                    // customPrint.printout("Queue peek compatible with existing locks");

                    if (itemLockTable.get(item).size() == 0) {
                        storeLock(peekOp);
                        // customPrint.printout("success");
                        // customPrint.printout("itemLockTable " + itemLockTable);
                    } else {
                        Lock oldLock = findTransLock(peekOp.getArg(), peekOp.getTID());
                        if (oldLock != null) {
                            oldLock.updateType();
                        } else {
                            storeLock(peekOp);
                        }
                        // customPrint.printout("success");
                        // customPrint.printout("itemLockTable " + itemLockTable);
                    }

                    unblockingSite.add(peekOp.getSID());
                    // customPrint.printout("Unblocking site: " + peekOp.getSID());
                    itemQueue.poll();
                    if (itemQueue.size() == 0) {
                        queueTable.remove(item);
                        break;
                    }
                } else {
                    // customPrint.printout("Queue peek not compatible with existing locks");
                    break;
                }
            }
        }
        siteLockTable.remove(siteID);
        customPrint.printout("Going to unblock site " + unblockingSite);
        return unblockingSite;
    }

    private void waitForGraphInit() {
        waitForGraph = new HashMap<>();
        for (Map.Entry<String, Queue<Operation>> waitingOpsEntry : queueTable.entrySet()) {
            String item = waitingOpsEntry.getKey();
            Queue<Operation> waitingOps = waitingOpsEntry.getValue();
            List<Lock> existingLocks = itemLockTable.get(item);
            for (Lock lock : existingLocks) {
                for (Operation op : waitingOps) {
                    if (op.getSID() != lock.getSID()) {
                        if (waitForGraph.containsKey(op.getSID())
                                && !waitForGraph.get(op.getSID()).contains(lock.getSID())) {
                            waitForGraph.get(op.getSID()).add(lock.getSID());
                        } else {
                            List<Integer> lockDenpendencies = new ArrayList<>();
                            lockDenpendencies.add(lock.getSID());
                            waitForGraph.put(op.getSID(), lockDenpendencies);
                        }
                    }
                }

            }
        }
    }

    public Integer hasDeadLock() {
        customPrint.printout("checking deadlock...");
        waitForGraphInit();
        // customPrint.printout("waitforgraph " + waitForGraph);
        // customPrint.printout("Site Lock table " + siteLockTable);
        // customPrint.printout("Item Lock table " + itemLockTable);
        // customPrint.printout("Queue table " + queueTable);
        // customPrint.printout("Lock table: " + siteLockTable);
        for (Map.Entry<Integer, List<Integer>> depends : waitForGraph.entrySet()) {
            Integer siteID = depends.getKey();
            int deadLockSite = hasDeadLockRecursive(siteID, new HashMap<>());
            if (deadLockSite > -1) {
                deadlockCount++;
                customPrint.printout("Deadlock Found! (total found " + deadlockCount);
                return deadLockSite;
            }
        }
        return -1;
    }

    private Integer hasDeadLockRecursive(Integer siteID, Map<Integer, Boolean> visited) {
        // customPrint.printout("visiting site " + siteID);

        if (visited.getOrDefault(siteID, false)) {
            return siteID;
        }
        visited.put(siteID, true);
        for (int j = 0; j < waitForGraph.getOrDefault(siteID, new ArrayList<>()).size(); j++) {
            // customPrint.printout("visited: " + visited);
            int deadLockSite = hasDeadLockRecursive(waitForGraph.get(siteID).get(j), visited);
            if (deadLockSite > -1) {
                return deadLockSite;
            }
        }
        return -1;
    }

}
