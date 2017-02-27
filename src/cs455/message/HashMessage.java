package cs455.message;

import java.util.Random;

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
	
	public void generateNewPayload() {
		new Random().nextBytes(payload);
	}

}
