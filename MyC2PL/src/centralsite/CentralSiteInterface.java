package centralsite;

import java.rmi.Remote;
import java.rmi.RemoteException;

import elements.Operation;

public interface CentralSiteInterface extends Remote {
    int getSiteId() throws RemoteException;

    Boolean requestLock(Operation op) throws RemoteException;

    Boolean releaseLock(Operation op) throws RemoteException;

}
