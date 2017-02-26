package cs455.scaling.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerListener implements Runnable {

	private ServerSocketChannel serverSocketChannel;
	private final Selector[] selectors;
	private final int threadPoolSize;
	private int socketChannelCount;
	private final int serverPort;
	private boolean shutDown = false;
	private final boolean debug;
	
	public ServerListener(int serverPort, int threadPoolSize, boolean debug) {
		this.serverPort = serverPort;
		this.debug = debug;
		this.threadPoolSize = threadPoolSize;
		this.selectors = new Selector[threadPoolSize];
		for (Selector selector: selectors) {				// Intialize all the selectors needed for the server
			try {
				selector = Selector.open();
			} catch (IOException e) {
				System.out.println(e);
			}
		}
		this.socketChannelCount = 0;
	}
	
	@Override
	public void run() {
		
		// Open a Server Socket channel
		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(serverPort));
			//serverSocketChannel.configureBlocking(false);
			if (debug) System.out.println(" Server socket channel opened.\n\tAddress: " + serverSocketChannel.socket().getInetAddress() + "\n\tPort: " + serverSocketChannel.socket().getLocalPort());
		} catch (IOException e) {
			System.out.println(e);
		}
		
		if (debug) System.out.println(" Server socket channel waiting for incoming connections...");
		while (!shutDown) {
			try {
				SocketChannel socketChannel = serverSocketChannel.accept();
				
				if (socketChannel != null) {
					if (debug) System.out.println(" New connection detected. Socket channel created.");
					registerChannelToSelector(socketChannel);
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
		
		if (debug) System.out.println(" Server listener thread exiting.");
	}
	
	// Channels are registered to selectors which have the fewest keys to load balance them 
	private synchronized void registerChannelToSelector(SocketChannel socketChannel) {
		socketChannelCount++;
		int channelsPerThread = socketChannelCount / threadPoolSize;
		if (debug) System.out.println(" Registering new channel to a selector. There will be at least " + channelsPerThread + " channels per thread");
		
	}

}
