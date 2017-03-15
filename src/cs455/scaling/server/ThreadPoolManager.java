package cs455.scaling.server;

import java.util.LinkedList;

import cs455.scaling.server.tasks.Task;
import cs455.util.StatTracker;

public class ThreadPoolManager implements Runnable {
	
	private int threadPoolSize;
	private StatTracker statTracker;
	private boolean debug;
	private WorkerThread[] threadPool;
	private Thread[] threadPoolThreads;
	private LinkedList<Task> taskQueue;
	private LinkedList<WorkerThread> idleThreads;
	private boolean shutDown;
	public int idleThreadCount;
	public int pendingTaskCount;
	
	public ThreadPoolManager(int threadPoolSize, StatTracker statTracker, boolean debug) {
		this.threadPoolSize = threadPoolSize;
		this.statTracker = statTracker;
		this.debug = true;
		this.threadPool = new WorkerThread[threadPoolSize];
		this.threadPoolThreads = new Thread[threadPoolSize];
		this.taskQueue = new LinkedList<Task>();
		this.idleThreads = new LinkedList<WorkerThread>();
		this.shutDown = false;
		this.idleThreadCount = 0;
		this.pendingTaskCount = 0;
	}

	@Override
	public void run() {
		if (debug) System.out.println("Thread pool manager started.");
		startAllWorkerThreads();
		
		long start = System.nanoTime();
		while (!shutDown) {
			start = printTPMdebug(start, debug);
			
			synchronized(taskQueue){
				synchronized(idleThreads){
					idleThreadCount = idleThreads.size();
					pendingTaskCount = taskQueue.size();
					if (taskQueue.size() > 0 && idleThreads.size() > 0) {
						if (debug) System.out.println("Matching pending task to idle thread...");
						WorkerThread taskedWorker = idleThreads.removeFirst();
						Task nextTask = taskQueue.removeFirst();
						taskedWorker.assignTask(nextTask);
						synchronized(taskedWorker){
							taskedWorker.notify();
						}
					}
				}
			}
		}
	}
	
	private long printTPMdebug(long start, boolean print){
		// Print TPM statistics every 5 seconds
		if (System.nanoTime() - start >= (5000000000L)) {
			if (print) System.out.println("TPM has " + idleThreads.size() + " idle threads, " + taskQueue.size() + " pending tasks.");
			start = System.nanoTime();
			statTracker.resetRW();
		}
		return start;
	}
	
	private void startAllWorkerThreads(){
		if (debug) System.out.println(" Starting worker threads.");
		for (int i = 0; i < threadPoolSize; i++){
			threadPool[i] = new WorkerThread(idleThreads, taskQueue, i, statTracker, debug);
			threadPoolThreads[i] = new Thread(threadPool[i]);
			threadPoolThreads[i].start();
		}
	}

	public void enqueueTask(Task task) {
		synchronized(taskQueue){
			if (debug) System.out.println("TPM enqueuing new task");
			taskQueue.add(task);
		}
	}

}
