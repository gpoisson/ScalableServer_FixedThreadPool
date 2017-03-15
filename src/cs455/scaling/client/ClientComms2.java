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

public class ClientComms2 {

	SocketChannel socketChannel;					// Socket channel connected to the server
	private final String serverHostname;			// Server IP address
	private final int serverPort;					// Server port number
	private final int messageRate;					// Number of messages to send per second
	private final HashComputer hashComputer;		// Object that computes hash codes of byte arrays
	private final LinkedList<String> hashCodes;		// Queue of hash codes waiting to be received from the server
	private final StatTracker statTracker;			// Accumulates statistics to be printed to console
	private ByteBuffer buffer;
	private boolean shutDown;						// Shut down switch
	private final boolean debug;					// Debug mode
	
	public ClientComms2(String serverHostname, int serverPort, int messageRate, HashComputer hashComputer, LinkedList<String> hashCodes, boolean debug) throws IOException {
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		this.messageRate = messageRate;
		this.shutDown = false;
		this.hashComputer = hashComputer;
		this.hashCodes = hashCodes;
		this.statTracker = new StatTracker();
		this.debug = debug;
	}
		
	public void startClient() throws IOException {
		if (debug) System.out.println("ClientComms starting the client...");
		long start = System.nanoTime();

		// Configure socket channel for connection to server
		socketChannel = SocketChannel.open();
		socketChannel.connect(new InetSocketAddress(serverHostname, serverPort));
		System.out.println("Client connected to server: " + socketChannel.getRemoteAddress());
		
		while (!shutDown){
			// Print client statistics every 10 seconds
			if (System.nanoTime() - start >= (10000000000L)) {
				Calendar calendar = Calendar.getInstance();
				Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
				System.out.println(currentTimestamp + "\tTotal Sent Count: " + statTracker.getWriteCount() + "\tTotal Received Count: " + statTracker.getReadCount());   
				start = System.nanoTime();
				statTracker.resetRW();
			}
			
			HashMessage hashMessage = new HashMessage();
			String sha = hashComputer.SHA1FromBytes(hashMessage.getPayload()).trim();
			
			statTracker.incrementHashes();

			if (debug) System.out.println(" Client has new message. Hash: " + sha + " added to hash code queue.");
			
			hashCodes.add(sha);

			// Load message payload into buffer
			ByteBuffer buffer = ByteBuffer.allocate(hashMessage.getPayload().length);
			buffer.put(hashMessage.getPayload());
			buffer.rewind();
			
			// Write message to socket channel
			int read = 0;
			while (buffer.hasRemaining() && read != -1){
				read = socketChannel.write(buffer);
			}
			statTracker.incrementWrites();
			
			buffer.clear();
			buffer = ByteBuffer.allocate(40);
			
			// Read server response
			read = 0;
			read = socketChannel.read(buffer);
			statTracker.incrementReads();
			
			buffer.rewind();
			
			// Verify server response
			String receivedSHA = new String();
			while (buffer.hasRemaining()){
				receivedSHA += (char) buffer.get();
			}
			
			if (debug) System.out.println("Client received hash code: " + receivedSHA.length() + " chars.\tVerified: " + verifyReceivedHash(receivedSHA));
			
			buffer = null;

			// Sleep until time to send next message
			long waitTime = (long) (1000.0/messageRate);
			if (debug) System.out.println(" Client waiting for " + (waitTime/1000) + " seconds...");
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
	}
	
	// Compare a received hash code to the hash code which the client expected to receive
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
}
