package cs455.scaling;

public class ThreadPoolManager implements Runnable {
	
	private Thread[] threadPool;
	private boolean debug;
	
	// ThreadPoolManager runs on its own thread. It builds and manages
	//   the thread pool.
	public ThreadPoolManager(int threadPoolSize, boolean debug) {
		this.debug = debug;
		threadPool = new Thread[threadPoolSize];
	}
	
	// Populates the thread pool with task objects
	public synchronized void populateThreadPool() {
		if (debug) System.out.println(" Populating thread pool with " + threadPool.length + " threads.");
		for (int id = 0; id < threadPool.length; id++) {
			threadPool[id] = new Thread(new WorkerThread(id, debug));
		}
	}
	
	// Executes all the thread objects in the thread pool
	public synchronized void startThreadPool() {
		if (debug) System.out.println(" Executing the threads in the thread pool...");
		for (int id = 0; id < threadPool.length; id++) {
			threadPool[id].start();
		}
	}

	@Override
	public void run() {
		populateThreadPool();
		startThreadPool();
	}

}
