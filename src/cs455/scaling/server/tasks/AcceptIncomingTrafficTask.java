package cs455.scaling.server.tasks;

import java.nio.channels.SelectionKey;

public class AcceptIncomingTrafficTask extends Task {

	public AcceptIncomingTrafficTask(SelectionKey key) {
		taskType = 1;
	}
}
