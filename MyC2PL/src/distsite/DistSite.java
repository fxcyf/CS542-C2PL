package distsite;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import centralsite.CentralSiteInterface;
import elements.Operation;
import elements.Transaction;

public class DistSite implements DistSiteInterface {

    private int siteId;

    private CentralSiteInterface centralSiteStub;

    private DataProcessor DP;

    private TransactionManager TM;

    public DistSite(String centralSiteHost, int centralSitePort, String DB_URL, String transaction_file) {

        try {
            // finds server in rmiregistry
            Registry registry = LocateRegistry.getRegistry(centralSiteHost, centralSitePort);
            centralSiteStub = (CentralSiteInterface) registry.lookup("CentralSite");

            siteId = centralSiteStub.getSiteId();
            DP = new DataProcessor(DB_URL);
            DP.initializeData();
            TM = new TransactionManager(siteId, transaction_file);
            TM.load_history();
            DistSiteInterface stub = (DistSiteInterface) UnicastRemoteObject.exportObject(this, 0);
            registry.bind("DistSite" + siteId, stub);

            System.err.println(String.format("Distributed site %d ready", siteId));

        } catch (Exception e) {
            System.err.println("Distributed site exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                Transaction transaction = TM.getNextTransaction();

                List<Operation> ops = transaction.getOperations();

                for (Operation op : ops) {
                    switch (op.getType()) {
                        case "read": {
                            Boolean res = centralSiteStub.requestLock(op);
                            if (res) {
                                int val = DP.read(op.getArg());
                                transaction.getReadValues().put(op.getArg(), val);
                            } else {
                                siteBlock();
                            }

                        }
                        case "write": {
                            Boolean res = centralSiteStub.requestLock(op);
                            if (res) {
                                DP.execute(op);
                            }
                            
                            break;
                        }
                        case "compute": {

                        }
                        case "commit": {
                            centralSiteStub.releaseLock(op);
                        }

                    }
                }
            } catch (Exception e) {
                System.err.println("Distributed site exception: " + e.toString());
                e.printStackTrace();
            }

        }
    }

    public void unblock() {

    }

    public void abort() {

    }

    public void update() {

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

        DistSite site = new DistSite(centralSiteHost, centralSitePort, DB_URL, transaction_file);

        site.run();

    }

}
