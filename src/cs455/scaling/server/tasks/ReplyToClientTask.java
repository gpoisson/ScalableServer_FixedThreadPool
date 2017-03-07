package cs455.scaling.server.tasks;

import java.nio.channels.SelectionKey;

public class ReplyToClientTask extends Task {

	public ReplyToClientTask(SelectionKey key) {
		taskType = 3;
	}
}
