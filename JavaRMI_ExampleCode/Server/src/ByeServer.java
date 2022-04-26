import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ByeServer extends Remote {
    
	String sayBye() throws RemoteException;
}