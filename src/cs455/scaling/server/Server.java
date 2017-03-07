package cs455.scaling.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

import cs455.scaling.Node;
import cs455.scaling.server.tasks.AcceptIncomingTrafficTask;
import cs455.scaling.server.tasks.ReplyToClientTask;

public class Server implements Node {

	private final int serverPort;
	private final int threadPoolSize;
	private final ThreadPoolManager tpManager;
	private final Thread tpManagerThread;
	private final int bufferSize;
	private Selector selector;
	private final ByteBuffer buffer;
	private boolean shutDown;
	
	private Server(int serverPort, int threadPoolSize) {
		this.serverPort = serverPort;
		this.threadPoolSize = threadPoolSize;
		this.tpManager = new ThreadPoolManager(this.threadPoolSize, debug);
		this.tpManagerThread = new Thread(this.tpManager);
		this.shutDown = false;
		this.buffer = ByteBuffer.allocate(60);
		this.bufferSize = 60;
	}
	
	public static void main(String[] args) throws IOException {
		
		// Check arguments
		if (args.length < 2) {
			System.out.println(usage());
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
		
		// Open selector
		server.selector = Selector.open();
		
		// Configure a Server Socket channel
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().bind(new InetSocketAddress(server.serverPort));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(server.selector, SelectionKey.OP_ACCEPT);
        
		if (debug) System.out.println(" Server socket channel opened.\n\tAddress: " + serverSocketChannel.socket().getInetAddress() + "\n\tPort: " + serverSocketChannel.socket().getLocalPort());
		if (debug) System.out.println(" Server socket channel waiting for incoming connections...");
		
		server.tpManagerThread.start();
		
		while (!server.shutDown) {
			if (debug) System.out.println(" Server selector waiting for new incoming connections...");
			
			server.selector.select();
			
			if (debug) System.out.println(" Server selector connected...");

			Iterator keys = server.selector.selectedKeys().iterator();
			
			if (debug) System.out.println(" Server selector has new keys...");
			
			while (keys.hasNext()) {
				int waitTime = 1000;
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				SelectionKey key = (SelectionKey) keys.next();
				//synchronized(key){
					if (key.isAcceptable()) {
						if (debug) System.out.println(" Connection accepted by server socket channel...");
						server.accept(key);
					}
					else if (key.isReadable() && key.attachment() == null) {
						if (debug) System.out.println(" Channel ready for reading...");
						AcceptIncomingTrafficTask readTask = new AcceptIncomingTrafficTask(key);
						if (debug) System.out.println(" Passing new read task to thread pool manager...");
						server.tpManager.enqueueTask(readTask);
					}
					else if (key.isWritable() && key.attachment() == null) {
						if (debug) System.out.println(" Channel ready for writing...");
						ReplyToClientTask writeTask = new ReplyToClientTask(key);
						if (debug) System.out.println(" Passing new write task to thread pool manager...");
						server.tpManager.enqueueTask(writeTask);
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
		//int interests = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
		//clientChannel.register(this.selector, interests);
		clientChannel.register(this.selector, SelectionKey.OP_READ);
		if (debug) System.out.println("Incoming connection registered with server selector");
	}
	
	private static String usage() {
		return "Usage:  Server <portnum> <thread-pool-size>";
	}

}
