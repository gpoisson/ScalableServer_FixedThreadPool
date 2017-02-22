package cs455.scaling.server;

import cs455.scaling.Node;

public class Server implements Node {

	public int serverPort;
	private int threadPoolSize;
	private Thread tpManager;
	
	private Server() {

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
		
		Thread sl = new Thread(new ServerListener(server.serverPort, debug));
		sl.start();
		server.tpManager = new Thread(new ThreadPoolManager(server.threadPoolSize, debug));
		server.tpManager.start();
	}
	
	private static String usage() {
		return "Usage:  Server <portnum> <thread-pool-size>";
	}

}
