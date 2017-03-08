package cs455.message;

import java.util.Random;

// Hash message object produced by Clients 
public class HashMessage {
	
	private byte[] payload;
	private int payloadSizeBytes;
	
	public HashMessage() {
		payloadSizeBytes = 8192;
		payload = new byte[payloadSizeBytes];
		generateNewPayload();
	}
	
	// Generates a copy of the payload and returns it
	public byte[] getPayload() {
		byte[] copyOfPayload = new byte[payloadSizeBytes];
		copyOfPayload = payload;
		return copyOfPayload;
	}
	
	// Generate new random payload
	public void generateNewPayload() {
		new Random().nextBytes(payload);
	}

}
