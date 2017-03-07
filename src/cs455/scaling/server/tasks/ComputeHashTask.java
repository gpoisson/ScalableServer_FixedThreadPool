package cs455.scaling.server.tasks;

public class ComputeHashTask extends Task {

	private byte[] data;
	
	public ComputeHashTask(byte[] packet) {
		this.data = packet;
	}
	
	public byte[] getBytes() {
		byte[] dataCopy = new byte[data.length];
		dataCopy = data;
		return dataCopy;
	}
}
