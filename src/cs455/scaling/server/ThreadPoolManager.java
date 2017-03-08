package cs455.scaling.server;

import java.util.LinkedList;

import cs455.scaling.server.WorkerThread;
import cs455.scaling.server.tasks.ReplyToClientTask;
import cs455.scaling.server.tasks.Task;
import cs455.util.StatTracker;

public class ThreadPoolManager implements Runnable {
	
	private final WorkerThread[] workerThreads;					// References to worker thread objects
	private final Thread[] threadPool;							// References to running worker threads
	private final LinkedList<Task> taskQueue;					// FIFO task queue
	private final LinkedList<WorkerThread> idleThreads;			// FIFO queue for idle threads
	private final StatTracker statTracker;
	private final boolean debug;
	private boolean shutDown;
	
	// ThreadPoolManager runs on its own thread. It builds and manages
	//   the thread pool.
	public ThreadPoolManager(int threadPoolSize, StatTracker statTracker, boolean debug) {
		this.debug = debug;
		this.shutDown = false;
		workerThreads = new WorkerThread[threadPoolSize];
		threadPool = new Thread[threadPoolSize];
		taskQueue = new LinkedList<Task>();
		this.statTracker = statTracker;
		idleThreads = new LinkedList<WorkerThread>();
		if (debug) System.out.println(" Thread pool constructed");
	}
	
	// Populates the thread pool with worker threads
	private synchronized void populateThreadPool() {
		if (debug) System.out.println(" Populating thread pool with " + threadPool.length + " threads.");
		for (int id = 0; id < threadPool.length; id++) {
			workerThreads[id] = new WorkerThread(idleThreads, id, statTracker, debug);
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
	
	// Enqueues a new task into the task queue, where it will wait to eventually be assigned to an idle worker thread
	public void enqueueTask(Task newTask) {
		synchronized(taskQueue) {
			taskQueue.add(newTask);
			if (debug) System.out.println(" Thread pool manager enqueuing new task... there are now " + taskQueue.size() + " queued tasks and " + idleThreads.size() + " idle threads...");
		}
	}
	
	public int getIdleThreadCount() {
		return idleThreads.size();
	}
	
	public int getTaskQueueSize() {
		return taskQueue.size();
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
			if (debug) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (debug) System.out.println("  Thread pool manager --  Task Queue: " + taskQueue.size() + "   Idle Threads: " + idleThreads.size());
			if (idleThreads.size() > 0) {
				// Check for ready reply tasks
				synchronized(idleThreads) {
					for (WorkerThread idle: idleThreads) {
						ReplyToClientTask newReply = idle.extractPendingReplyTask();
						if (newReply != null) {
							if (debug) System.out.println("New reply task detected by thread pool manager. Adding to task queue...");
							synchronized(taskQueue){
								taskQueue.add(newReply);
							}
							if (debug) System.out.println("Task queue size is now: " + taskQueue.size());
						}
					}
				}
				if (taskQueue.size() > 0) {
					if (debug) System.out.println("  Thread pool manager detects idle threads and pending tasks.");
					WorkerThread idleThread = retrieveIdleThread();
					synchronized(idleThread) {
						synchronized(taskQueue) {
							if (debug) System.out.println(" Matching retrieved idle thread with a pending task.");
							//synchronized (taskQueue) {
							idleThread.assignTask(taskQueue.removeFirst());
							synchronized(idleThread.sleepLock) {
								idleThread.sleepLock.notify();
							}
							if (debug) System.out.println(" Thread and task matched. Task queue size is now: " + taskQueue.size());
						}
					}
				}
			}
		}
	}

	public boolean getWTThreadStatus() {
		return threadPool[1].isAlive();
	}

}
