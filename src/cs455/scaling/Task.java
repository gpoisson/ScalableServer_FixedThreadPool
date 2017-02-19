package cs455.scaling;

public class Task implements Runnable {

	private final int taskId;
	private boolean debug;
	private boolean shutDown;
	
	public Task(int id, boolean debug) {
		this.debug = debug; 
		this.taskId = id;
		this.shutDown = false;
	}
	
	@Override
	public void run() {
		if (debug) System.out.println("  New thread " + taskId + " executed.");
		while (!shutDown) {
			
		}
	}

}
