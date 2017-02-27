package cs455.scaling.server;

import java.util.ArrayList;
import java.util.LinkedList;

import cs455.scaling.server.WorkerThread;
import cs455.scaling.server.tasks.ComputeHashTask;
import cs455.scaling.server.tasks.Task;

public class ThreadPoolManager implements Runnable {
	
	private final WorkerThread[] workerThreads;					// References to worker thread objects
	private final Thread[] threadPool;							// References to running worker threads
	private final LinkedList<Task> taskQueue;					// FIFO task queue
	private final LinkedList<WorkerThread> idleThreads;			// FIFO queue for idle threads
	private final boolean debug;
	private boolean shutDown;
	
	// ThreadPoolManager runs on its own thread. It builds and manages
	//   the thread pool.
	public ThreadPoolManager(int threadPoolSize, boolean debug) {
		this.debug = debug;
		this.shutDown = false;
		workerThreads = new WorkerThread[threadPoolSize];
		threadPool = new Thread[threadPoolSize];
		taskQueue = new LinkedList<Task>();
		idleThreads = new LinkedList<WorkerThread>();
		if (debug) System.out.println(" Thread pool constructed");
	}
	
	// Populates the thread pool with worker threads
	private synchronized void populateThreadPool() {
		if (debug) System.out.println(" Populating thread pool with " + threadPool.length + " threads.");
		for (int id = 0; id < threadPool.length; id++) {
			workerThreads[id] = new WorkerThread(idleThreads, id, debug);
			threadPool[id] = new Thread(workerThreads[id]);
		}
	}
	
	// Executes all the worker threads in the thread pool
	private synchronized void startThreadPool() {
		if (debug) System.out.println(" Executing the threads in the thread pool...");
		for (int id = 0; id < threadPool.length; id++) {
			threadPool[id].start();
		}
	}
	
	// Retrieves the worker thread which has been idle the longest from the queue
	private WorkerThread retrieveIdleThread() {
		if (idleThreads.size() > 0) {
			synchronized (idleThreads) {
				WorkerThread idleThread = idleThreads.removeFirst();
				if (debug) System.out.println(" Idle thread " + idleThread.getId() + " retrieved.");
				return idleThread;
			}
		}
		return null;
	}

	@Override
	public void run() {
		// Populate thread pool with worker threads
		populateThreadPool();
		
		// Execute the worker threads
		startThreadPool();
		
		// Begin monitoring for idle threads
		if (debug) System.out.println(" Thread pool manager now monitoring for idle worker threads and pending tasks...");
		while (!shutDown) {
			if ((idleThreads.size() > 0) && (taskQueue.size() > 0)) {
				WorkerThread idleThread = retrieveIdleThread();
				synchronized(idleThread) {
					if (debug) System.out.println(" Matching retrieved idle thread with a pending task.");
					idleThread.notify();
					idleThread.assignTask(taskQueue.removeFirst());
					if (debug) System.out.println(" Thread and task matched. Task queue size is now: " + taskQueue.size());
				}
			}
		}
	}

}
