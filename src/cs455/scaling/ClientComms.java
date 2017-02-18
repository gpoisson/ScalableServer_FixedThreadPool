package cs455.scaling;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class ClientComms implements Runnable {
	
	private String host;
	private int portNum;
	private SocketChannel socketChannel;
	private boolean shutDown = false;
	private boolean debug;
	
	public ClientComms(String host, int portNum, boolean debug) {
		this.host = host;
		this.portNum = portNum;
		this.debug = debug;
	}
	
	public synchronized void connectToServer() {
		if (debug) System.out.println(" Client connecting to server...");
		try {
			socketChannel = SocketChannel.open();
			socketChannel.connect(new InetSocketAddress(host, portNum));
			if (debug) System.out.println(" Client connected to server successfully.");
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public synchronized void disconnectFromServer() {
		shutDown = true;
		if (debug) System.out.println(" Client disconnecting from server...");
		try {
			socketChannel.close();
			if (debug) System.out.println(" Client disconnected from server successfully.");
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	@Override
	public void run() {
		if (debug) System.out.println(" Client communications initializing...");
		connectToServer();
		while (!shutDown) {
			
		}
		disconnectFromServer();
	}

}
