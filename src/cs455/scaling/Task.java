package cs455.scaling;

public class Task implements Runnable {

	private int taskId;
	private boolean debug;
	
	public Task(int id, boolean debug) {
		this.debug = debug; 
		this.taskId = id;
	}
	
	@Override
	public void run() {
		if (debug) System.out.println("  New thread " + taskId + " executed.");
	}

}
