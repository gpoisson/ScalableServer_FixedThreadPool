package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;
import cs455.message.HashMessage;
import cs455.util.HashComputer;
import cs455.util.StatTracker;

public class NIOClientComms {

	SocketChannel socketChannel;
	private final String serverHostname;
	private final int serverPort;
	private final int messageRate;
	private final HashComputer hashComputer;
	private final LinkedList<String> hashCodes;
	private final StatTracker statTracker;
	private boolean shutDown;
	private final boolean debug;
	
	public NIOClientComms(String serverHostname, int serverPort, int messageRate, HashComputer hashComputer, LinkedList<String> hashCodes, boolean debug) throws IOException {
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		this.messageRate = messageRate;
		this.shutDown = false;
		this.hashComputer = hashComputer;
		this.hashCodes = hashCodes;
		this.statTracker = new StatTracker();
		this.debug = debug;
		socketChannel = SocketChannel.open();
	}
		
	public void startClient() throws IOException {
		if (debug) System.out.println("NIOClientComms starting the client...");
		long start = System.nanoTime();
		//SelectionKey key = socketChannel.register(selector, SelectionKey.OP_CONNECT);
		//connect(key);
		socketChannel.connect(new InetSocketAddress(serverHostname, serverPort));
		System.out.println("Client connected to server: " + socketChannel.getRemoteAddress());
		while (!shutDown){
			
			if (System.nanoTime() - start >= (10000000000L)) {
				Calendar calendar = Calendar.getInstance();
				Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
				System.out.println(currentTimestamp + "\tTotal Sent Count: " + statTracker.getWriteCount() + "\tTotal Received Count: " + statTracker.getReadCount());   
				start = System.nanoTime();
				statTracker.resetRW();
			}
			
			ByteBuffer buffer = ByteBuffer.allocate(8192);
			HashMessage hashMessage = new HashMessage();
			String sha = hashComputer.SHA1FromBytes(hashMessage.getPayload()).trim();
			statTracker.incrementHashes();
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
			statTracker.incrementWrites();
			if (debug) System.out.println(" Clearing buffer.");
			buffer.clear();
			
			int read = 0;
			if (debug) System.out.println(" Reading from socket channel to buffer...");
			read = socketChannel.read(buffer);
			statTracker.incrementReads();
			if (debug) System.out.println("...Data read from channel.  read: " + read + " bytes.");
			buffer.rewind();
			
			byte[] receiveHash = new byte[read]; 
			for (int i = 0; i < read; i++){
				receiveHash[i] = buffer.get();
			}
			String receivedHashString = new String();
			for (byte b: receiveHash){
				receivedHashString += (char) b;
			}

			receiveHash = null;
			buffer.rewind();
	
			if (debug) System.out.println(" Client received msg from server: " + receivedHashString);
			if(verifyReceivedHash(receivedHashString)){
				if (debug) System.out.println(" Client verified received hash!");
			}
			else{
				System.out.println(" Client failed to verify received hash.");
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
		String nextExpectedHash = ((String) hashCodes.removeFirst()).trim();
		receivedHash = receivedHash.trim();
		if (receivedHash.equals(nextExpectedHash)) return true;
		else {
			System.out.println(" Expected hash: " + nextExpectedHash);
			System.out.println(" Received hash: " + receivedHash);
			return false;
		}
	}

	/*
	private void connect (SelectionKey key) throws IOException {
		socketChannel.connect(new InetSocketAddress(serverHostname, serverPort));
		if (debug) System.out.println("  Client connect() finishing connect; setting interest to WRITE");
		SocketChannel channel = (SocketChannel) key.channel();
		channel.finishConnect();
		key.interestOps(SelectionKey.OP_WRITE);
	}
	*/
}
