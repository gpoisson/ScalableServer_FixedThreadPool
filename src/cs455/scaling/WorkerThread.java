package cs455.scaling;

import java.nio.channels.ServerSocketChannel;

public class WorkerThread implements Runnable {

	private final int workerThreadID;
	private boolean debug;
	private boolean shutDown;
	
	private ServerSocketChannel ssChannel;
	
	public WorkerThread(int id, boolean debug) {
		this.debug = debug; 
		this.workerThreadID = id;
		this.shutDown = false;
	}
	
	@Override
	public void run() {
		if (debug) System.out.println("  New worker thread " + workerThreadID + " executed.");
		while (!shutDown) {
			
		}
	}

}
