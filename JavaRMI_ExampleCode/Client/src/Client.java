
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Client implements ByeServer {

    private Client() {}

	public String sayBye() throws RemoteException {
		return "Bye, Server!";
	}
    
    public static void main(String[] args) {
    // register client
	try {
	    Client obj = new Client();
	    //registers itself in rmiregistry
	    ByeServer stub = (ByeServer) UnicastRemoteObject.exportObject(obj, 0);

	    // Bind the remote object's stub in the registry
	    Registry registry = LocateRegistry.getRegistry();
	    registry.bind("ByeServer", stub);

	    System.err.println("Client ready");
	} catch (Exception e) {
	    System.err.println("Client exception: " + e.toString());
	    e.printStackTrace();
	}
    // call server
	String host = (args.length < 1) ? null : args[0];
	try {
	    // finds server in rmiregistry
		Registry registry = LocateRegistry.getRegistry(host);
	    Hello stub = (Hello) registry.lookup("Hello");
	    // asks server to perform sayHello() function
	    String response = stub.sayHello();
	    System.out.println("Server response: " + response);
	} catch (Exception e) {
	    System.err.println("Client exception: " + e.toString());
	    e.printStackTrace();
	}
    }

}