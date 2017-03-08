package cs455.scaling.server.tasks;

import java.nio.channels.SelectionKey;

// Task created when new data is available to be read
public class AcceptIncomingTrafficTask extends Task {
	
	private SelectionKey key;

	public AcceptIncomingTrafficTask(SelectionKey key) {
		this.key = key;
	}
	
	public SelectionKey getKey() {
		return key;
	}
}
