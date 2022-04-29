package centralsite;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import distsite.DistSiteInterface;
import elements.Operation;
import elements.Transaction;
import utils.customPrint;

public class CentralSite implements CentralSiteInterface {

    private int port;
    private int siteCount;
    LockManager lockManager;

    public CentralSite(int port, int deadlockPeriod) {
        lockManager = new LockManager();
        siteCount = 0;
        this.port = port;

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkAndResolveDeadLock();
            }
        }, 0, deadlockPeriod);
    }

    public int distSiteReg() {
        siteCount += 1;
        return siteCount;
    }

    public Boolean requestLock(Operation op) {
        return lockManager.requestLock(op);

    }

    public void releaseLock(Transaction trans) {
        try {
            Registry registry = LocateRegistry.getRegistry(port);
            for (int i = 1; i <= siteCount; i++) {
                DistSiteInterface distSiteStub = (DistSiteInterface) registry.lookup("DistSite" + i);
                distSiteStub.update(trans.getPreCommit());
            }
        } catch (Exception e) {
            customPrint.printerr("Central site exception: " + e.toString());
            e.printStackTrace();
        }
        List<Integer> unblockingSite = lockManager.releaseLock(trans);
        unblockSite(unblockingSite);
    }

    private void unblockSite(List<Integer> unblockingSite) {

        try {
            Registry registry = LocateRegistry.getRegistry(port);
            for (Integer siteID : unblockingSite) {
                DistSiteInterface distSiteStub = (DistSiteInterface) registry.lookup("DistSite" + siteID);
                distSiteStub.unblock();
            }

        } catch (Exception e) {
            customPrint.printout("Central site exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public void checkAndResolveDeadLock() {
        Integer deadlockSite = lockManager.hasDeadLock();
        if (deadlockSite > -1) {
            List<Integer> unblockingSite = lockManager.releaseSiteLock(deadlockSite);
            unblockSite(unblockingSite);
            try {
                Registry registry = LocateRegistry.getRegistry(port);
                DistSiteInterface distSiteStub = (DistSiteInterface) registry.lookup("DistSite" + deadlockSite);
                customPrint.printout("Going to abort transaction in site " + deadlockSite);
                distSiteStub.abort();
            } catch (Exception e) {
                customPrint.printerr("Central site exception: " + e.toString());
                e.printStackTrace();
            }
        }

    }

    /**
     * Main function of central site
     * [0] export port
     * [1] check deadlock period
     * 
     * @param args
     */

    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        int port = Integer.parseInt(args[0]);
        int deadlockPeriod = Integer.parseInt(args[1]);

        CentralSite centralSite = new CentralSite(port, deadlockPeriod);

        try {

            CentralSiteInterface stub = (CentralSiteInterface) UnicastRemoteObject.exportObject(centralSite, 0);

            Registry registry = LocateRegistry.getRegistry(port);
            registry.bind("CentralSite", stub);

            customPrint.printerr("Central site ready");

        } catch (Exception e) {
            customPrint.printerr("Central site exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
