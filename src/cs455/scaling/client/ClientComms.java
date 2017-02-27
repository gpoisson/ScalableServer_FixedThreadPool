package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import cs455.message.HashMessage;

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
		this.messageRate = messageRate;
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
		
		HashMessage hashMessage = new HashMessage();
		ByteBuffer buffer = ByteBuffer.allocate(hashMessage.getPayload().length);
		while (!shutDown) {
			buffer.clear();
			buffer.put(hashMessage.getPayload());
			
			buffer.flip();
			while(buffer.hasRemaining()){
				try {
					socketChannel.write(buffer);
					if (debug) System.out.println("Hash message written to socket channel successfully.");
				} catch (IOException e) {
					System.out.println(" Client communications thread has failed to write to the socket channel.");
					System.out.println(e);
				}
			}
			if (debug) System.out.println(" Client communications thread suspending for " + (1.0/messageRate) + " seconds.");
			try {
				Thread.sleep((long) (1.0/messageRate));
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
		disconnectFromServer();
	}

}
