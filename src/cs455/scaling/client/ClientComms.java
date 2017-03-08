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

public class ClientComms {

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
	
	public ClientComms(String serverHostname, int serverPort, int messageRate, HashComputer hashComputer, LinkedList<String> hashCodes, boolean debug) throws IOException {
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
		if (debug) System.out.println("NIOClientComms starting the client...");
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
			buffer = ByteBuffer.allocate(hashMessage.getPayload().length);
			buffer.rewind();
			buffer.put(hashMessage.getPayload());
			buffer.rewind();
			
			// Write message to socket channel
			if (debug) System.out.println(" Writing from buffer to socket channel...");
			socketChannel.write(buffer);
			statTracker.incrementWrites();
			if (debug) System.out.println(" Clearing buffer.");
			buffer.clear();
			
			// Read response hash message from server
			int read = 0;
			if (debug) System.out.println(" Reading from socket channel to buffer...");
			read = socketChannel.read(buffer);
			statTracker.incrementReads();
			if (debug) System.out.println("...Data read from channel.  read: " + read + " bytes.");
			buffer.rewind();
			
			// Store response in byte array
			byte[] receiveHash = new byte[read]; 
			for (int i = 0; i < read; i++){
				receiveHash[i] = buffer.get();
			}
			String receivedHashString = new String();
			for (byte b: receiveHash){
				receivedHashString += (char) b;
			}

			// Remove unneeded data
			receiveHash = null;
			buffer = null;
	
			// Verify received hash against hash code in hash queue
			if (debug) System.out.println(" Client received msg from server: " + receivedHashString);
			if(verifyReceivedHash(receivedHashString)){
				if (debug) System.out.println(" Client verified received hash!");
			}
			else{
				if (debug) System.out.println(" Client failed to verify received hash.");
			}

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
			if (debug) System.out.println(" Expected hash: " + nextExpectedHash);
			if (debug) System.out.println(" Received hash: " + receivedHash);
			return false;
		}
	}
}
