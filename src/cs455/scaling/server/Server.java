package cs455.scaling.server;

import cs455.scaling.Node;

public class Server implements Node {

	private final int serverPort;
	private final int threadPoolSize;
	private final Thread tpManager;
	
	private Server(int serverPort, int threadPoolSize) {
		this.serverPort = serverPort;
		this.threadPoolSize = threadPoolSize;
		this.tpManager = new Thread(new ThreadPoolManager(this.threadPoolSize, debug));
	}
	
	public static void main(String[] args) {
		
		// Check arguments
		if (args.length < 2) {
			usage();
			System.exit(0);
		}
		
		Server server = new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
		
		System.out.println("New server initialized.\tPort: " + server.serverPort + "\tThread Pool Size: " + server.threadPoolSize);
		
		Thread serverListener = new Thread(new ServerListener(server.serverPort, debug));
		serverListener.start();
		server.tpManager.start();
	}
	
	private static String usage() {
		return "Usage:  Server <portnum> <thread-pool-size>";
	}

}
