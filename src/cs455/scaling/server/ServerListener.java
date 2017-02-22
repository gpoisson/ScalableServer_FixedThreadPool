package cs455.scaling.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerListener implements Runnable {

	private ServerSocketChannel serverSocketChannel;
	private final int serverPort;
	private boolean shutDown = false;
	private final boolean debug;
	
	public ServerListener(int serverPort, boolean debug) {
		this.serverPort = serverPort;
		this.debug = debug;
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
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
		
		if (debug) System.out.println(" Server listener thread exiting.");
	}

}
