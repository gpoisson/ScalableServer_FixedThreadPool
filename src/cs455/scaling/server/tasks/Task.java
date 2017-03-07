package cs455.scaling.server.tasks;

import java.nio.channels.SelectionKey;

/*
 * 1. Read Message
 * 2. Compute hash
 * 3. Write Message
 */
public abstract class Task {
	
	public abstract SelectionKey getKey();
}


