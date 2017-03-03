package cs455.scaling.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import cs455.scaling.Node;

public class Server implements Node {

	private final int serverPort;
	private final int threadPoolSize;
	private final ThreadPoolManager tpManager;
	private final Thread tpManagerThread;
	private final int bufferSize;
	private Selector selector;
	private boolean shutDown;
	
	private Server(int serverPort, int threadPoolSize) {
		this.serverPort = serverPort;
		this.threadPoolSize = threadPoolSize;
		this.tpManager = new ThreadPoolManager(this.threadPoolSize, debug);
		this.tpManagerThread = new Thread(this.tpManager);
		this.shutDown = false;
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
		
		// Open a Server Socket channel
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().bind(new InetSocketAddress(server.serverPort));
		if (debug) System.out.println(" Server socket channel opened.\n\tAddress: " + serverSocketChannel.socket().getInetAddress() + "\n\tPort: " + serverSocketChannel.socket().getLocalPort());

		if (debug) System.out.println(" Server socket channel waiting for incoming connections...");
		
		server.tpManagerThread.start();
		
		while (!server.shutDown) {
			if (debug) System.out.println(" Server selector waiting for incoming connections...");
			server.selector.select();
			if (debug) System.out.println(" Server selector connected...");

			Iterator keys = server.selector.selectedKeys().iterator();
			if (debug) System.out.println(" Server selector has new keys...");
			while (keys.hasNext()) {
				SelectionKey key = (SelectionKey) keys.next();
				if (key.isAcceptable()) {
					server.accept(key);
					if (debug) System.out.println(" Server accepted new connection");
				}
				else if (key.isReadable()) {
					if (debug) System.out.println(" Server reading...");
					server.read(key);
				}
			}
			
			/*
			if (socketChannel != null) {
				if (debug) System.out.println(" New connection detected. Socket channel created.");
			}
			
			server.tpManager.passNewSocketChannel(socketChannel);
			*/
		}
	}
	
	private void accept(SelectionKey key) throws IOException {
		ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
		SocketChannel channel = serverSocket.accept();
		
		System.out.println("Accepting incoming connection");
		channel.configureBlocking(false);
		channel.register(this.selector, SelectionKey.OP_READ);
	}
	
	private void read(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		int read = 0;
		try {
			while (buffer.hasRemaining() && read != -1) {
				read = channel.read(buffer);
			}
		} catch (IOException e) {
			// Abnormal termination
			
			/*
			 *  ADD SERVER DISCONNECT HERE
			 *  server.disconnect(key);
			 *  return;
			 */
		}
		if (read == -1) {
			// Connection terminated by client
			
			/*
			 *  server.disconnect(key);
			 *  return;
			 */
		}
		key.interestOps(SelectionKey.OP_WRITE);
	}
	
	private void write(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		// Data stored in 'data' of type: byte[]
		/*ByteBuffer buffer = ByteBuffer.wrap(data);
		 * channel.write(buffer);
		 * key.interestOps(SelectionKey.OP_READ);
		 */
	}
	
	private static String usage() {
		return "Usage:  Server <portnum> <thread-pool-size>";
	}

}
