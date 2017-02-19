package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

public class ClientComms implements Runnable {
	
	private String host;
	private int portNum;
	private int messageRate;
	private SocketChannel socketChannel;
	private boolean shutDown = false;
	private boolean debug;
	
	public ClientComms(String host, int portNum, int messageRate, boolean debug) {
		this.host = host;
		this.portNum = portNum;
		this.debug = debug;
	}
	
	public synchronized void connectToServer() {
		if (debug) System.out.println(" Client connecting to server...");
		try {
			SocketAddress address = new InetSocketAddress(host, portNum);
			socketChannel = SocketChannel.open(address);
			//socketChannel.connect();
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
		
		//Thread msgEngine = new Thread(new MessageEngine(socketChannel, messageRate, debug));
		//msgEngine.start();
		MessageEngine msgEngine = new MessageEngine(socketChannel, messageRate, debug);
		while (!shutDown) {
			try {
				msgEngine.wait(messageRate);
			} catch (InterruptedException e) {
				System.out.println(e);
			}
			if (debug) System.out.println("  Message engine woken up. Sending a new message...");
		}
		disconnectFromServer();
	}

}
