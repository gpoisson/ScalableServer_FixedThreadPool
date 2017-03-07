package cs455.scaling.server.tasks;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class AcceptIncomingTrafficTask extends Task {
	
	private SelectionKey key;

	public AcceptIncomingTrafficTask(SelectionKey key) {
		this.key = key;
	}
	
	public SelectionKey getKey() {
		return key;
	}
}
