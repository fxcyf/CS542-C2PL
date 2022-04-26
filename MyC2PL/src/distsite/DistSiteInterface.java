package distsite;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface DistSiteInterface extends Remote {

    void unblock() throws RemoteException;

    void abort() throws RemoteException;

    void update(Map<String, Integer> updateValues) throws RemoteException;
}
