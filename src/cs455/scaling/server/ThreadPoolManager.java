package cs455.scaling.server;

import java.util.LinkedList;

import cs455.scaling.WorkerThread;
import cs455.scaling.server.tasks.Task;

public class ThreadPoolManager implements Runnable {
	
	private final Thread[] threadPool;
	private LinkedList<Task> taskQueue;
	private LinkedList<WorkerThread> idleThreads;
	private final boolean debug;
	private boolean shutDown;
	
	// ThreadPoolManager runs on its own thread. It builds and manages
	//   the thread pool.
	public ThreadPoolManager(int threadPoolSize, boolean debug) {
		this.debug = debug;
		this.shutDown = false;
		threadPool = new Thread[threadPoolSize];
		taskQueue = new LinkedList<Task>();
		idleThreads = new LinkedList<WorkerThread>();
	}
	
	// Populates the thread pool with task objects
	private synchronized void populateThreadPool() {
		if (debug) System.out.println(" Populating thread pool with " + threadPool.length + " threads.");
		for (int id = 0; id < threadPool.length; id++) {
			threadPool[id] = new Thread(new WorkerThread(idleThreads, id, debug));
		}
	}
	
	// Executes all the thread objects in the thread pool
	private synchronized void startThreadPool() {
		if (debug) System.out.println(" Executing the threads in the thread pool...");
		for (int id = 0; id < threadPool.length; id++) {
			threadPool[id].start();
		}
	}

	@Override
	public void run() {
		populateThreadPool();
		startThreadPool();
		if (debug) System.out.println(" Thread pool manager now monitoring for idle worker threads and pending tasks...");
		while (!shutDown) {
			if ((idleThreads.size() > 0) && (taskQueue.size() > 0)) {
				if (debug) System.out.println(" Idle thread and pending task detected. Assigning pending task to idle thread.");
				idleThreads.removeFirst().assignTask(taskQueue.removeFirst());
			}
		}
	}

}
