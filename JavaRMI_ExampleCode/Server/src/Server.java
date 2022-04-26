import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
	
public class Server implements Hello {
	
	ByeServer stub;
	
    public Server() {}

    public String sayHello() {
    	
    	// looks up client in rmiregistry
		this.regClient();
		
		try {
			// invokes servers function sayBye()
			String response = this.stub.sayBye();
			System.out.println("Client response: " + response);
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
    	
    	// reply client
        return "Hello, Client!";
    }
    
    public void regClient() {
    	// find client
    	try {
    	    Registry registry = LocateRegistry.getRegistry(null);
    	    stub = (ByeServer) registry.lookup("ByeServer");

    	} catch (Exception e) {
    	    System.err.println("Client exception: " + e.toString());
    	    e.printStackTrace();
    	}
    }
	
    public static void main(String args[]) {
    	
		Server obj = new Server();
		try {
		    
		    Hello stub = (Hello) UnicastRemoteObject.exportObject(obj, 0);
		
		    // Bind the remote object's stub in the registry
		    Registry registry = LocateRegistry.getRegistry();
		    registry.bind("Hello", stub);
		
		    System.err.println("Server ready");
		} catch (Exception e) {
		    System.err.println("Server exception: " + e.toString());
		    e.printStackTrace();
		}
	
    }
}