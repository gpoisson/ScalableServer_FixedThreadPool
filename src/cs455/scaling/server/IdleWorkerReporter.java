package cs455.scaling.server;

import java.util.LinkedList;
import cs455.scaling.WorkerThread;

public class IdleWorkerReporter {
	
	private final LinkedList<WorkerThread> idleThreads;
	
	public IdleWorkerReporter(LinkedList<WorkerThread> idleThreads) {
		this.idleThreads = idleThreads;
	}
	
	public synchronized void reportIdle(WorkerThread idleThread) {
		
	}

}
