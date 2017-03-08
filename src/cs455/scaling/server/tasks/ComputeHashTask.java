package cs455.scaling.server.tasks;

import java.nio.channels.SelectionKey;

// Task created when data is available to be hashed by the server
public class ComputeHashTask extends Task {

	private byte[] data;
	private SelectionKey key;
	
	public ComputeHashTask(SelectionKey key, byte[] packet) {
		this.data = packet;
		this.key = key;
	}
	
	public byte[] getBytes() {
		byte[] dataCopy = new byte[data.length];
		dataCopy = data;
		return dataCopy;
	}
	
	public SelectionKey getKey(){
		return this.key;
	}
}
