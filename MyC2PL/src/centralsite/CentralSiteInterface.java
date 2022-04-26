package centralsite;

import java.rmi.Remote;
import java.rmi.RemoteException;

import elements.Operation;
import elements.Transaction;

public interface CentralSiteInterface extends Remote {
    int distSiteReg() throws RemoteException;

    Boolean requestLock(Operation op) throws RemoteException;

    void releaseLock(Transaction trans) throws RemoteException;

}
