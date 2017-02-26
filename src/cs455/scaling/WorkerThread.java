package cs455.scaling;

import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;

import cs455.scaling.server.tasks.Task;

public class WorkerThread implements Runnable {

	private final int workerThreadID;
	private Task currentTask;
	private LinkedList<WorkerThread> idleThreads;
	private boolean debug;
	private boolean shutDown;
	private boolean idle;
	
	public Object sleepLock;
	
	private ServerSocketChannel ssChannel;
	
	public WorkerThread(LinkedList<WorkerThread> idleThreads, int id, boolean debug) {
		this.debug = debug; 
		this.workerThreadID = id;
		this.shutDown = false;
		this.idleThreads = idleThreads;
		this.currentTask = null;
		this.sleepLock = new Object();
	}
	
	@Override
	public void run() {
		if (debug) System.out.println("  New worker thread " + workerThreadID + " executed.");
		while (!shutDown) {
			if (currentTask != null) {
				if (debug) System.out.println("  Worker thread " + workerThreadID + " has a task.");
			}
			else {
				reportIdle();
			}
		}
	}
	
	// Allows the thread pool manager to monitor for idle worker threads
	private void reportIdle() {
		synchronized(idleThreads){
			if (!idle) {
				if (debug) System.out.println("  Worker thread " + workerThreadID + " reporting itself idle to the thread pool manager.");
				idle = true;
				idleThreads.add(this);
			}
		}
		if (idle) {
			synchronized(sleepLock) {
				try {
					sleepLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean isIdle() {
		if (idle) return true;
		else return false;
	}
	
	public int getId() {
		return this.workerThreadID;
	}
	
	public synchronized void assignTask(Task newTask) {
		if (debug) System.out.println("Worker thread " + workerThreadID + " accepting new task...");
		if (newTask != null) {
			idle = false;
			currentTask = newTask;
		}
	}
}
