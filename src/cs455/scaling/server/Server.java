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
		
		
		/*
		 * TA RECOMMENDS MAKING SERVER LISTENER AN OBJECT IN THE MAIN THREAD,
		 * NOT SURE IF IT'S TOTALLY NECESSARY 
		 * 
		 * OTHER TIPS:
		 * 1. OP_ACCEPT is needed in server listener to accept conections
		 * 2. Channels are writable until their send buffers (not BYTE BUFFER) are full (even if key is not set with OP_WRITE)
		 * 3. Set OP_WRITE - key.isWritable() = true in next iteration
		 * 4. Immediately after accepting connection and creating a new socket channel, register it with
		 *    the selector and set OP_READ, since we assume there will be data coming from the client
		 * 5. After the write is complete, set the interest back to OP_READ
		 * 6. Be careful using BYTE BUFFER - any operations (read/get/put) advances the pointer
		 *      Use rewind() before writing to channel -- resets pointer to 0 so that all data is written
		 *      Use clear() before reading from channel -- empties the current BYTE BUFFER to make room for new data
		 *      
		 * 
		 */
		Thread serverListener = new Thread(new ServerListener(server.serverPort, debug));
		serverListener.start();
		server.tpManager.start();
	}
	
	private static String usage() {
		return "Usage:  Server <portnum> <thread-pool-size>";
	}

}
