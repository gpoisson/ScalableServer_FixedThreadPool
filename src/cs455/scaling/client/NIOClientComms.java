package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

import cs455.message.HashMessage;
import cs455.util.HashComputer;

public class NIOClientComms {

	SocketChannel socketChannel;
	private final String serverHostname;
	private final int serverPort;
	private final int messageRate;
	private final ByteBuffer buffer;
	private final HashComputer hashComputer;
	private final LinkedList hashCodes;
	private Selector selector;
	private boolean shutDown;
	private final boolean debug;
	
	public NIOClientComms(String serverHostname, int serverPort, int messageRate, HashComputer hashComputer, LinkedList<String> hashCodes, boolean debug) throws IOException {
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		this.messageRate = messageRate;
		this.shutDown = false;
		this.buffer = ByteBuffer.allocate(8192);
		this.hashComputer = hashComputer;
		this.hashCodes = hashCodes;
		this.debug = debug;
		selector = Selector.open();
		socketChannel = SocketChannel.open();
	}
		
	public void startClient() throws IOException {
		if (debug) System.out.println("NIOClientComms starting the client...");
		//SelectionKey key = socketChannel.register(selector, SelectionKey.OP_CONNECT);
		//connect(key);
		socketChannel.connect(new InetSocketAddress(serverHostname, serverPort));
		if (debug) System.out.println("Client connected to server: " + socketChannel.getRemoteAddress());
		while (!shutDown){
			HashMessage hashMessage = new HashMessage();
			String sha = hashComputer.SHA1FromBytes(hashMessage.getPayload());
			if (debug) System.out.println(" Client has new message. Hash: " + sha + " added to hash code queue.");
			hashCodes.add(sha);
			//String testString = "test string";
			//buffer.wrap(hashMessage.getPayload());
			buffer.rewind();
			buffer.put(hashMessage.getPayload());
			buffer.rewind();
			//while (buffer.hasRemaining()){
			//	System.out.print((char) buffer.get());
			//}
			//buffer.rewind();
			//if (debug) System.out.println(" Loaded buffer with data: " + buffer.array().toString());
			//buffer.rewind();
			if (debug) System.out.println(" Writing from buffer to socket channel...");
			socketChannel.write(buffer);
			if (debug) System.out.println(" Clearing buffer.");
			buffer.clear();
			if (debug) System.out.println(" Reading from socket channel to buffer...");
			socketChannel.read(buffer);
			String receivedHash = new String();
			buffer.rewind();
			while (buffer.hasRemaining()) {
				receivedHash += (char) buffer.get();
			}
			if (debug) System.out.println(" Client received msg from server: " + receivedHash);
			if(verifyReceivedHash(receivedHash)){
				if (debug) System.out.println(" Client verified received hash!");
			}
			else{
				if (debug) System.out.println(" Client failed to verify received hash........");
			}
			buffer.clear();
			long waitTime = (long) (1000.0/messageRate);
			if (debug) System.out.println(" Client waiting for " + (waitTime/1000) + " seconds...");
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
	}
	
	private boolean verifyReceivedHash(String receivedHash) {
		String nextExpectedHash = (String) hashCodes.removeFirst();
		return (receivedHash.equals(nextExpectedHash));
	}

	private void connect (SelectionKey key) throws IOException {
		socketChannel.connect(new InetSocketAddress(serverHostname, serverPort));
		if (debug) System.out.println("  Client connect() finishing connect; setting interest to WRITE");
		SocketChannel channel = (SocketChannel) key.channel();
		channel.finishConnect();
		key.interestOps(SelectionKey.OP_WRITE);
	}
}
