package cs455.scaling.server.tasks;

import java.nio.channels.SelectionKey;

// Task created when a hash is ready to be returned to the client
public class ReplyToClientTask extends Task {
	
	private SelectionKey key;
	private String replyHash;

	public ReplyToClientTask(SelectionKey key, String replyHash) {
		this.key = key;
		this.replyHash = replyHash;
	}
	
	public SelectionKey getKey() {
		return key;
	}
	
	public String getReplyHash() {
		String reply = new String();
		reply = replyHash;
		return reply;
	}
}
