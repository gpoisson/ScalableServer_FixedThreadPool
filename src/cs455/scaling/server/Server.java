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
				long waitTime = (long) 1000.0;
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				SelectionKey key = (SelectionKey) keys.next();
				if (key.isAcceptable()) {
					if (debug) System.out.println(" Connection accepted by server socket channel...");
					
					//SocketChannel socketChannel = serverSocketChannel.accept();
					//socketChannel.configureBlocking(false);
					//socketChannel.register(server.selector, SelectionKey.OP_READ);
					server.accept(key);
				}
				else if (key.isConnectable()) {
					if (debug) System.out.println(" Connection established with remote server");
					
				}
				if (key.isReadable()) {
					if (debug) System.out.println(" Channel ready for reading...");
					
                    //server.read(key);
					AcceptIncomingTrafficTask readTask = new AcceptIncomingTrafficTask(key);
					if (debug) System.out.println(" Passing new read task to thread pool manager...");
					server.tpManager.enqueueTask(readTask);
				}
				if (key.isWritable()) {
					if (debug) System.out.println(" Channel ready for writing...");
					//socketChannel.write(buffer);
                    //buffer.clear();
					//server.write(key);
					ReplyToClientTask writeTask = new ReplyToClientTask(key);
					if (debug) System.out.println(" Passing new write task to thread pool manager...");
					server.tpManager.enqueueTask(writeTask);
				}
				keys.remove();
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
		SocketChannel clientChannel = serverSocket.accept();
		
		if (debug) System.out.println("Accepted incoming connection");
		clientChannel.configureBlocking(false);
		int interests = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
		clientChannel.register(this.selector, interests);
		if (debug) System.out.println("Incoming connection registered with server selector");
	}
	
	private void read(SelectionKey key) throws IOException {
		if (debug) System.out.println("Reading data from channel...");
		SocketChannel clientChannel = (SocketChannel) key.channel();
		//ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		int read = 0;
		try {
			while (buffer.hasRemaining() && read != -1) {
				read = clientChannel.read(buffer);
                //buffer.flip();
			}
			if (debug) System.out.println("...Data read from channel.  read: " + read);
			buffer.rewind();
			while (buffer.hasRemaining()){
				if (debug) System.out.print((char) buffer.get());
			}
			if (debug) System.out.println();
			buffer.flip();
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
		//if (debug) System.out.println(" Switching key interest to WRITE");
		//key.interestOps(SelectionKey.OP_WRITE);
	}
	
	private void write(SelectionKey key) throws IOException {
		SocketChannel clientChannel = (SocketChannel) key.channel();
		int read = 0;
		buffer.rewind();
		String testResponse = "test response";
		buffer.put(testResponse.getBytes());
		buffer.rewind();
		try {
			while (buffer.hasRemaining() && read != -1) {
				read = clientChannel.write(buffer);
                //buffer.flip();
			}
			if (debug) System.out.println("...Data written to channel.  read: " + read);
			buffer.rewind();
			while (buffer.hasRemaining()){
				if (debug) System.out.print((char) buffer.get());
			}
			if (debug) System.out.println();
			buffer.flip();
		} catch (IOException e) {
			// Abnormal termination
			
			/*
			 *  ADD SERVER DISCONNECT HERE
			 *  server.disconnect(key);
			 *  return;
			 */
		}
		// Data stored in 'data' of type: byte[]
		/*ByteBuffer buffer = ByteBuffer.wrap(data);
		 * channel.write(buffer);
		 * key.interestOps(SelectionKey.OP_READ);
		 */
		//if (debug) System.out.println(" Switching key interest to READ");
		//key.interestOps(SelectionKey.OP_READ);
	}
	
	private static String usage() {
		return "Usage:  Server <portnum> <thread-pool-size>";
	}

}
