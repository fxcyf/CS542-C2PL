package distsite;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DistSiteInterface extends Remote {

    void unblock() throws RemoteException;

    void abort() throws RemoteException;

    void update() throws RemoteException;
}
