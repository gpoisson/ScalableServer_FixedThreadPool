package cs455.scaling;

public class Server implements Node {

	public int serverPort;
	private int threadPoolSize;
	private Thread serverListener;
	
	private Server() {
		
		serverListener = new Thread(new ServerListener(serverPort, debug));
		
	}
	
	public static void main(String[] args) {
		
		Server server = new Server();
		
		// Parse command arguments
		if (args.length > 1) {
			server.serverPort = Integer.parseInt(args[0]);
			server.threadPoolSize = Integer.parseInt(args[1]);
		}
		else {
			System.out.println(usage());
			System.exit(0);
		}
		
		System.out.println("New server initialized.\tPort: " + server.serverPort + "\tThread Pool Size: " + server.threadPoolSize);
		
		server.serverListener.start();
		
	}
	
	private static String usage() {
		return "Usage:  Server <portnum> <thread-pool-size>";
	}

}
