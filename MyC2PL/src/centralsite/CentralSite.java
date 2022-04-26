package centralsite;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import elements.Operation;
import elements.Transaction;

public class CentralSite implements CentralSiteInterface {

    private int siteCount;
    LockManager lockManager;

    public CentralSite() {
        lockManager = new LockManager();
        siteCount = 0;
    }

    public int distSiteReg() {
        siteCount += 1;
        return siteCount;
    }

    public Boolean requestLock(Operation op) {

        return lockManager.requestLock(op);

    }

    public void releaseLock(Transaction trans) {
        lockManager.releaseLock(trans);
        // return true;
    }

    /**
     * Main function of central site
     * [0] export port
     * 
     * 
     * @param args
     */

    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        int port = Integer.parseInt(args[0]);

        CentralSite centralSite = new CentralSite();

        try {

            CentralSiteInterface stub = (CentralSiteInterface) UnicastRemoteObject.exportObject(centralSite, 0);

            Registry registry = LocateRegistry.getRegistry(port);
            registry.bind("CentralSite", stub);

            System.err.println("Central site ready");

        } catch (Exception e) {
            System.err.println("Central site exception: " + e.toString());
            e.printStackTrace();
        }

    }
}
