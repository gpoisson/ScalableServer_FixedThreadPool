package cs455.scaling.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Iterator;

import cs455.scaling.Node;
import cs455.scaling.server.tasks.AcceptIncomingTrafficTask;
import cs455.util.StatTracker;

public class Server implements Node {

	private final int serverPort;					// Port through which clients will connect to server
	private final int threadPoolSize;				// Fixed size of server thread pool
	private final ThreadPoolManager tpManager;		// Thread pool manager object
	private final Thread tpManagerThread;			// Thread pool manager thread
	private Selector selector;						// Server selector to monitor open channels
	private StatTracker statTracker;				// Maintain throughput and connection stats
	private boolean shutDown;						// Shut down switch
	
	private Server(int serverPort, int threadPoolSize) {
		this.serverPort = serverPort;
		this.threadPoolSize = threadPoolSize;
		this.statTracker = new StatTracker();
		this.tpManager = new ThreadPoolManager(this.threadPoolSize, this.statTracker, debug);
		this.tpManagerThread = new Thread(this.tpManager);
		this.shutDown = false;
	}
	
	public static void main(String[] args) throws IOException {
		
		long start = System.nanoTime();
		
		// Check arguments
		if (args.length < 2) {
			System.out.println(usage());
			System.exit(0);
		}
		
		// Instantiate a server
		Server server = new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
		
		System.out.println("New server initialized.\tPort: " + server.serverPort + "\tThread Pool Size: " + server.threadPoolSize);
		
		// Open selector
		server.selector = Selector.open();
		
		// Configure a Server Socket channel
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().bind(new InetSocketAddress(server.serverPort));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(server.selector, SelectionKey.OP_ACCEPT);
        
		if (debug) System.out.println(" Server socket channel opened.\n\tAddress: " + serverSocketChannel.socket().getInetAddress() + "\n\tPort: " + serverSocketChannel.socket().getLocalPort());
		if (debug) System.out.println(" Server socket channel waiting for incoming connections...");
		
		// Execute the thread pool manager thread
		server.tpManagerThread.start();
		
		while (!server.shutDown) {
			// Print out server statistics every 5 seconds
			if (System.nanoTime() - start >= (5000000000L)) {
				Calendar calendar = Calendar.getInstance();
				Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
				int throughput = (server.statTracker.getThroughput() / 5);
				System.out.println(currentTimestamp + "\t   Current Server Throughput: " + throughput + " messages/s,\tActive Client Connections: " + server.statTracker.getConnections() + "\tIdle thread count: " + server.tpManager.getIdleThreadCount() + "\tTask queue size: " + server.tpManager.getTaskQueueSize() + "\tTPM alive: " + server.tpManagerThread.isAlive() + "\tWT[1] alive: " + server.tpManager.getWTThreadStatus());
				start = System.nanoTime();
				server.statTracker.resetRW();
			}
			
			if (debug) System.out.println(" Server selector waiting for new incoming connections...");
			
			server.selector.select();
			
			if (debug) System.out.println(" Server selector connected...");

			Iterator<SelectionKey> keys = server.selector.selectedKeys().iterator();
			
			if (debug) System.out.println(" Server selector has new keys...");
			
			while (keys.hasNext()) {
				if (debug) {
					int waitTime = 1000;
					try {
						Thread.sleep(waitTime);
					} catch (InterruptedException e) {
						System.out.println(e);
					}
				}
				
				SelectionKey key = (SelectionKey) keys.next();
				//synchronized(key){
					if (key.isAcceptable()) {
						if (debug) System.out.println(" Connection accepted by server socket channel...");
						server.accept(key);
						server.statTracker.incrementConnections();
					}
					if (key.isReadable()){
						if (debug) System.out.println(" Key readable...");
						if (key.attachment() == null) {
							if (debug) System.out.println(" Channel ready for reading...");
							AcceptIncomingTrafficTask readTask = new AcceptIncomingTrafficTask(key);
							if (debug) System.out.println(" Passing new read task to thread pool manager...");
							server.tpManager.enqueueTask(readTask);
							key.attach("temp");
						}
					}
					if (key.isWritable()) {
						if (debug) System.out.println(" Key writable...");
						if (key.attachment() == null) {
							if (debug) System.out.println(" Channel ready for writing...");
							//ReplyToClientTask writeTask = new ReplyToClientTask(key);
							//if (debug) System.out.println(" Passing new write task to thread pool manager...");
							//server.tpManager.enqueueTask(writeTask);
							//key.attach("temp");
						}
					}
				//}
				keys.remove();
			}
		}
	}
	
	private void accept(SelectionKey key) throws IOException {
		ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
		SocketChannel clientChannel = serverSocket.accept();
		
		if (debug) System.out.println("Accepted incoming connection");
		clientChannel.configureBlocking(false);
		int interests = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
		clientChannel.register(this.selector, interests);
		//clientChannel.register(this.selector, SelectionKey.OP_READ);
		if (debug) System.out.println("Incoming connection registered with server selector");
	}
	
	private static String usage() {
		return "Usage:  Server <portnum> <thread-pool-size>";
	}

}
