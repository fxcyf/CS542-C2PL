package distsite;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

import centralsite.CentralSiteInterface;
import elements.Operation;
import elements.Transaction;
import utils.customPrint;

public class DistSite implements DistSiteInterface {

    private int siteID;

    private volatile boolean blocked = false;
    private volatile boolean abortCurTrans = false;
    private CentralSiteInterface centralSiteStub;

    private DataProcessor DP;

    private TransactionManager TM;

    public DistSite(CentralSiteInterface centralSiteStub, int siteID, String DB_NAME, String transaction_file) {

        this.siteID = siteID;

        this.centralSiteStub = centralSiteStub;
        blocked = false;
        abortCurTrans = false;
        DP = new DataProcessor(DB_NAME);
        DP.DBInit();
        TM = new TransactionManager(siteID, transaction_file);

        try {
            TM.load_transactions();
        } catch (Exception e) {
            customPrint.printerr("Distributed site exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public void process() {
        while (true) {
            try {
                abortCurTrans = false;
                blocked = false;
                Transaction trans = TM.getNextTransaction();
                if (trans != null) {
                    customPrint.printout("Starting transaction " + trans.getTID());

                    List<Operation> ops = trans.getOperations();

                    for (Operation op : ops) {
                        switch (op.getType()) {
                            case Operation.readType: {
                                // customPrint.printout("read " + op.getArg());
                                if (!trans.hasLock(op)) {
                                    // customPrint.printout("Don't have lock for read item: " + op.getArg());
                                    Boolean res = centralSiteStub.requestLock(op);
                                    if (!res) {
                                        blocked = true;
                                        siteBlocked();
                                    }
                                    if (abortCurTrans) {
                                        break; // break switch
                                    }
                                    trans.receiveOrUpdateLock(op);
                                    int val = DP.read(op.getArg());
                                    trans.getStoredValues().put(op.getArg(), val);
                                } else {
                                    // customPrint.printout("Already has lock for item: " + op.getArg());
                                    int val;
                                    if (trans.getStoredValues().containsKey(op.getArg())) {
                                        val = trans.getStoredValues().get(op.getArg());
                                    } else {
                                        val = DP.read(op.getArg());
                                    }
                                    trans.getStoredValues().put(op.getArg(), val);
                                }
                                // customPrint.printout("Stored values: " + trans.getStoredValues());
                                break;

                            }
                            case Operation.writeType: {
                                // customPrint.printout("write " + op.getArg());

                                if (!trans.hasLock(op)) {
                                    // customPrint.printout("Don't have lock for write item: " + op.getArg());

                                    Boolean res = centralSiteStub.requestLock(op);
                                    if (!res) {
                                        blocked = true;
                                        siteBlocked();
                                    }
                                    if (abortCurTrans) {
                                        break; // break switch
                                    }
                                    trans.receiveOrUpdateLock(op);
                                    int val = trans.getStoredValues().get(op.getArg());
                                    trans.getPreCommit().put(op.getArg(), val);
                                } else {
                                    int val = trans.getStoredValues().get(op.getArg());
                                    trans.getPreCommit().put(op.getArg(), val);
                                }
                                // customPrint.printout("Stored values: " + trans.getStoredValues());
                                // customPrint.printout("Pre commit values: " + trans.getPreCommit());

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
                                // customPrint.printout("compute " + operand1 + operator + operand2);

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
                                // customPrint.printout("operandVal1: " + operandVal1);
                                // customPrint.printout("operandVal2: " + operandVal2);
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
                                // customPrint.printout("Stored values: " + trans.getStoredValues());

                                break;
                            }
                            case Operation.commitType: {
                                // customPrint.printout("commit");

                                centralSiteStub.releaseLock(trans);

                                customPrint.printout(String.format("Transaction %d completed", trans.getTID()));
                                // DP.display();
                                break;
                            }
                            default:
                                throw new Exception("Invalid operation");
                        }
                        if (abortCurTrans) {
                            customPrint.printout("Transaction " + trans.getTID() + " is aborted");
                            break; // break op loops
                        }
                    }

                } else {
                    while (true) {
                        customPrint.printout(String.format("Site %d no more transactions", siteID));
                        DP.display();
                        Thread.sleep(2000);
                    }
                }
            } catch (Exception e) {
                customPrint.printerr(String.format("Distributed site %d exception: %s", siteID, e.toString()));
                e.printStackTrace();
            }
        }
    }

    public void siteBlocked() {
        while (blocked) {
            try {
                customPrint.printout("Site " + siteID + " is blocked");
                Thread.sleep(1000);
            } catch (Exception e) {
                customPrint.printerr(String.format("Distributed site %d exception: %s", siteID, e.toString()));
                e.printStackTrace();
            }
        }
    }

    public void unblock() {
        blocked = false;
        customPrint.printout("Site " + siteID + " is now unblocked");
    }

    public void abort() {
        abortCurTrans = true;
        blocked = false;
        customPrint.printout("Site " + siteID + " abort current transaction");
    }

    public void update(Map<String, Integer> updateValues) {
        // customPrint.printout("Distributed Site " + siteID + " Updating database");
        DP.write(updateValues);
    }

    /**
     * Main function of distributed site.
     * parameters:
     * [0] Database name
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

        String DB_NAME = args[0];
        String centralSiteHost = args[1];
        int centralSitePort = Integer.parseInt(args[2]);
        String transaction_file = args[3];

        try {
            // finds server in rmiregistry
            Registry registry = LocateRegistry.getRegistry(centralSiteHost, centralSitePort);
            CentralSiteInterface centralSiteStub = (CentralSiteInterface) registry.lookup("CentralSite");
            int siteID = centralSiteStub.distSiteReg();
            DistSite site = new DistSite(centralSiteStub, siteID, DB_NAME, transaction_file);
            DistSiteInterface stub = (DistSiteInterface) UnicastRemoteObject.exportObject(site, 0);
            registry.bind("DistSite" + siteID, stub);
            customPrint.printout("Dist site stub: DistSite" + siteID);
            customPrint.printerr(String.format("Dist site %d ready", siteID));

            Thread.sleep(2000);

            site.process();

        } catch (Exception e) {

            customPrint.printerr("Distributed site exception: " + e.toString());
            e.printStackTrace();

        }

    }

}
