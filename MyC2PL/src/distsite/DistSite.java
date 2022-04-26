package distsite;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

import centralsite.CentralSiteInterface;
import elements.Operation;
import elements.Transaction;

public class DistSite implements DistSiteInterface, Runnable {

    private int siteID;

    private volatile boolean running = true;
    private volatile boolean blocked = false;

    private CentralSiteInterface centralSiteStub;

    private DataProcessor DP;

    private TransactionManager TM;

    public DistSite(CentralSiteInterface centralSiteStub, int siteID, String DB_URL, String transaction_file) {

        this.siteID = siteID;
        this.blocked = false;
        this.centralSiteStub = centralSiteStub;
        DP = new DataProcessor(DB_URL);
        DP.initializeData();
        TM = new TransactionManager(siteID, transaction_file);

        try {
            TM.load_history();
        } catch (Exception e) {
            System.err.println("Distributed site exception: " + e.toString());
            e.printStackTrace();
        }

    }

    public void process() {

        while (true) {

            try {
                Transaction trans = TM.getNextTransaction();
                List<Operation> ops = trans.getOperations();

                for (Operation op : ops) {
                    switch (op.getType()) {
                        case Operation.readType: {

                            if (!trans.hasLock(op)) {
                                Boolean res = centralSiteStub.requestLock(op);
                                if (res) {
                                    trans.receiveOrUpdateLock(op);
                                    int val = DP.read(op.getArg());
                                    trans.getStoredValues().put(op.getArg(), val);
                                } else {
                                    blocked = true;
                                    wait();
                                }
                            } else {
                                int val = DP.read(op.getArg());
                                trans.getStoredValues().put(op.getArg(), val);
                            }

                            break;

                        }
                        case Operation.writeType: {

                            if (!trans.hasLock(op)) {
                                Boolean res = centralSiteStub.requestLock(op);
                                if (res) {
                                    trans.receiveOrUpdateLock(op);
                                    int val = trans.getStoredValues().get(op.getArg());
                                    trans.getPreCommit().put(op.getArg(), val);
                                } else {
                                    blocked = true;
                                    wait();
                                }
                            } else {
                                int val = trans.getStoredValues().get(op.getArg());
                                trans.getPreCommit().put(op.getArg(), val);
                            }

                            break;
                        }
                        case Operation.mathType: {
                            String arg = op.getArg();
                            char operator = op.getOperator();
                            String operand1 = op.getOperand1();
                            String operand2 = op.getOperand2();
                            int operandVal1;
                            int operandVal2;
                            int res;
                            if (Character.isDigit(operand1.charAt(0))) {
                                operandVal1 = Integer.parseInt(operand1);
                            } else {
                                operandVal1 = trans.getStoredValues().get(operand1);
                            }
                            if (Character.isDigit(operand2.charAt(0))) {
                                operandVal2 = Integer.parseInt(operand2);
                            } else {
                                operandVal2 = trans.getStoredValues().get(operand2);
                            }
                            switch (operator) {
                                case '+': {
                                    res = operandVal1 + operandVal2;
                                    break;
                                }
                                case '-': {
                                    res = operandVal1 - operandVal2;
                                    break;
                                }
                                case '*': {
                                    res = operandVal1 * operandVal2;
                                    break;
                                }
                                case '/': {
                                    res = operandVal1 / operandVal2;
                                    break;
                                }
                                default:
                                    throw new Exception("Invalid operator: " + operator);
                            }
                            trans.getStoredValues().put(arg, res);
                        }
                        case Operation.commitType: {
                            centralSiteStub.releaseLock(trans);
                            System.out.println(String.format("Transaction %s completed", trans.getTID()));
                            trans.reset();
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println(String.format("Distributed site %d exception: %s", siteID, e.toString()));
                e.printStackTrace();
            }

        }
    }

    @Override
    public void run() {
        while (running) {
            synchronized (this) {
                if (!running) { // may have changed while waiting to
                    // synchronize on pauseLock
                    break;
                }
                if (blocked) {
                    try {
                        synchronized (this) {
                            this.wait(); // will cause this Thread to block until
                            // another thread calls pauseLock.notifyAll()
                            // Note that calling wait() will
                            // relinquish the synchronized lock that this
                            // thread holds on pauseLock so another thread
                            // can acquire the lock to call notifyAll()
                            // (link with explanation below this code)
                        }
                    } catch (Exception e) {
                        break;
                    }
                    if (!running) { // running might have changed since we paused
                        break;
                    }
                }
            }
            // Your code here
            try {
                Transaction trans = TM.getNextTransaction();
                List<Operation> ops = trans.getOperations();

                for (Operation op : ops) {
                    switch (op.getType()) {
                        case Operation.readType: {

                            if (!trans.hasLock(op)) {
                                Boolean res = centralSiteStub.requestLock(op);
                                if (res) {
                                    trans.receiveOrUpdateLock(op);
                                    int val = DP.read(op.getArg());
                                    trans.getStoredValues().put(op.getArg(), val);
                                } else {
                                    blocked = true;
                                    pause();
                                }
                            } else {
                                int val = DP.read(op.getArg());
                                trans.getStoredValues().put(op.getArg(), val);
                            }

                            break;

                        }
                        case Operation.writeType: {

                            if (!trans.hasLock(op)) {
                                Boolean res = centralSiteStub.requestLock(op);
                                if (res) {
                                    trans.receiveOrUpdateLock(op);
                                    int val = trans.getStoredValues().get(op.getArg());
                                    trans.getPreCommit().put(op.getArg(), val);
                                } else {
                                    blocked = true;
                                    pause();
                                }
                            } else {
                                int val = trans.getStoredValues().get(op.getArg());
                                trans.getPreCommit().put(op.getArg(), val);
                            }

                            break;
                        }
                        case Operation.mathType: {
                            String arg = op.getArg();
                            char operator = op.getOperator();
                            String operand1 = op.getOperand1();
                            String operand2 = op.getOperand2();
                            int operandVal1;
                            int operandVal2;
                            int res;
                            if (Character.isDigit(operand1.charAt(0))) {
                                operandVal1 = Integer.parseInt(operand1);
                            } else {
                                operandVal1 = trans.getStoredValues().get(operand1);
                            }
                            if (Character.isDigit(operand2.charAt(0))) {
                                operandVal2 = Integer.parseInt(operand2);
                            } else {
                                operandVal2 = trans.getStoredValues().get(operand2);
                            }
                            switch (operator) {
                                case '+': {
                                    res = operandVal1 + operandVal2;
                                    break;
                                }
                                case '-': {
                                    res = operandVal1 - operandVal2;
                                    break;
                                }
                                case '*': {
                                    res = operandVal1 * operandVal2;
                                    break;
                                }
                                case '/': {
                                    res = operandVal1 / operandVal2;
                                    break;
                                }
                                default:
                                    throw new Exception("Invalid operator: " + operator);
                            }
                            trans.getStoredValues().put(arg, res);
                        }
                        case Operation.commitType: {
                            centralSiteStub.releaseLock(trans);
                            System.out.println(String.format("Transaction %s completed", trans.getTID()));
                            trans.reset();
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println(String.format("Distributed site %d exception: %s", siteID, e.toString()));
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        running = false;
        // you might also want to interrupt() the Thread that is
        // running this Runnable, too, or perhaps call:
        unblock();
        // to unblock
    }

    public void pause() {
        // you may want to throw an IllegalStateException if !running
        blocked = true;
    }

    public void unblock() {
        synchronized (this) {
            blocked = false;
            this.notifyAll(); // Unblocks thread
        }
    }

    // public void unblock() {
    // blocked = false;
    // this.process();
    // }

    public void abort() {
        unblock();
    }

    public void update(Map<String, Integer> updateValues) {
        DP.write(updateValues);
    }

    /**
     * Main function of distributed site.
     * Expect parameters:
     * [0] Database url
     * [1] centralsite hostname
     * [2] centralsite port
     * [3] transaction file name
     * 
     * 
     * @param args
     */

    public static void main(String[] args) {

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        String DB_URL = args[0];
        String centralSiteHost = args[1];
        int centralSitePort = Integer.parseInt(args[2]);
        String transaction_file = args[3];

        try {
            // finds server in rmiregistry
            Registry registry = LocateRegistry.getRegistry(centralSiteHost, centralSitePort);
            CentralSiteInterface centralSiteStub = (CentralSiteInterface) registry.lookup("CentralSite");
            int siteID = centralSiteStub.distSiteReg();
            DistSite site = new DistSite(centralSiteStub, siteID, DB_URL, transaction_file);
            DistSiteInterface stub = (DistSiteInterface) UnicastRemoteObject.exportObject(site, 0);
            registry.bind("DistSite" + siteID, stub);

            System.err.println(String.format("Distributed site %d ready", siteID));

            Thread.sleep(2000);

            site.process();

        } catch (Exception e) {

            System.err.println("Distributed site exception: " + e.toString());
            e.printStackTrace();

        }

    }

}
