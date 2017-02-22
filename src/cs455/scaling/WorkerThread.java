package cs455.scaling;

import java.nio.channels.ServerSocketChannel;
import java.util.LinkedList;

import cs455.scaling.server.tasks.Task;

public class WorkerThread implements Runnable {

	private final int workerThreadID;
	LinkedList<WorkerThread> idleThreads;
	private Task currentTask;
	private boolean debug;
	private boolean shutDown;
	private boolean idle;
	
	private ServerSocketChannel ssChannel;
	
	public WorkerThread(LinkedList<WorkerThread> idleThreads, int id, boolean debug) {
		this.debug = debug; 
		this.workerThreadID = id;
		this.idleThreads = idleThreads;
		this.shutDown = false;
		this.currentTask = null;
	}
	
	@Override
	public void run() {
		if (debug) System.out.println("  New worker thread " + workerThreadID + " executed.");
		while (!shutDown) {
			if (currentTask != null) {
				if (debug) System.out.println("  New task assigned to worker thread " + workerThreadID);
			}
			else {
				reportIdle();
			}
		}
	}
	
	// Allows the thread pool manager to monitor for idle worker threads
	private synchronized void reportIdle() {
		if (debug) System.out.println("  Worker thread " + workerThreadID + " reporting itself idle to the thread pool manager.");
		idle = true;
		idleThreads.add(this);
	}
	
	public synchronized void assignTask(Task newTask) {
		if (newTask != null) {
			idle = false;
			currentTask = newTask;
		}
	}

}
