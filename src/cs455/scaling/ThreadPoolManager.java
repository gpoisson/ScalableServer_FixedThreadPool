package cs455.scaling;

public class ThreadPoolManager {
	
	private Thread[] threadPool;
	private boolean debug;
	
	public ThreadPoolManager(int threadPoolSize, boolean debug) {
		this.debug = debug;
		threadPool = new Thread[threadPoolSize];
	}
	
	public synchronized void populateThreadPool() {
		for (int id = 0; id < threadPool.length; id++) {
			threadPool[id] = new Thread(new Task(id, debug));
		}
	}
	
	public synchronized void startThreadPool() {
		for (int id = 0; id < threadPool.length; id++) {
			threadPool[id].start();
		}
	}

}
