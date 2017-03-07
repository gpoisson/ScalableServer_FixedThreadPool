package cs455.scaling.server.tasks;

import java.nio.channels.SelectionKey;

public class ReplyToClientTask extends Task {
	
	private SelectionKey key;

	public ReplyToClientTask(SelectionKey key) {
		this.key = key;
	}
	
	public SelectionKey getKey() {
		return key;
	}
}
