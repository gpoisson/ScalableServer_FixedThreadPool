package cs455.scaling.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import cs455.scaling.server.tasks.AcceptIncomingTrafficTask;
import cs455.scaling.server.tasks.ComputeHashTask;
import cs455.scaling.server.tasks.ReplyToClientTask;
import cs455.scaling.server.tasks.Task;
import cs455.util.HashComputer;
import cs455.util.StatTracker;

public class WorkerThread implements Runnable {
	private int workerThreadID;
	private StatTracker statTracker;
	private boolean debug;
	private LinkedList<WorkerThread> idleThreads;
	private boolean shutDown;
	public Object sleepLock;
	private Task currentTask;
	private LinkedList<Task> taskQueue;

	public WorkerThread(LinkedList<WorkerThread> idleThreads, LinkedList<Task> taskQueue, int id, StatTracker statTracker, boolean debug) {
		this.workerThreadID = id;
		this.statTracker = statTracker;
		this.debug = debug;
		this.idleThreads = idleThreads;
		this.shutDown = false;
		this.sleepLock = new Object();
		this.currentTask = null;
		this.taskQueue = taskQueue;
		//System.out.println("Worker Thread " + id + " constructed.");
	}

	@Override
	public void run() {
		if (debug) System.out.println("  New worker thread " + workerThreadID + " executed.");
		reportIdle();
		while (!shutDown) {
			if (currentTask == null){
				reportIdle();
			}
			else {
				if (debug) System.out.println("  Worker thread " + workerThreadID + " has a new task");
				performTask();
			}
		}
		
	}
	
	private void performTask(){
		if (debug) System.out.println(" Worker thread " + workerThreadID + " performing task...");
		synchronized (currentTask){
			if (currentTask instanceof AcceptIncomingTrafficTask){
				try {
					read();
				} catch (IOException e) {
					System.out.println(e);
				}
			}
			else if (currentTask instanceof ComputeHashTask){
				ReplyToClientTask newReplyTask = computeHash();
				synchronized(taskQueue){
					taskQueue.add(newReplyTask);
				}
			}
			else if (currentTask instanceof ReplyToClientTask){
				try {
					reply();
				} catch (IOException e) {
					System.out.println(e);
				}
			}
		}
	}
	
	private void read() throws IOException{
		if (debug) System.out.println("  READ TASK");
		ByteBuffer buffer = ByteBuffer.allocate(8192);
		SelectionKey key = currentTask.getKey();
		//System.out.println(key);
		SocketChannel socketChannel = (SocketChannel) key.channel();
		int read = 0;
		try{
			byte[] data = new byte[8192];
			//int i = 0;
			//System.out.println(buffer.position() + " A");
			while(buffer.hasRemaining() && read != -1){
				read = socketChannel.read(buffer);
			}
			//System.out.println(buffer.position() + " B");
			statTracker.incrementReads();
			if (debug) System.out.println(" Worker thread " + workerThreadID + " has received " + read + " bytes of data.");
			
			buffer.rewind();
			int byteCount = 0;
			for (int i = 0; i < 8192; i++){
				data[i] = buffer.get();
				byteCount++;
			}
			
			if (debug) System.out.println(byteCount + " total bytes read from client.");
			ComputeHashTask hashTask = new ComputeHashTask(key,data);
			taskQueue.add(hashTask);
			currentTask = null;
		} catch (NegativeArraySizeException e) {
			statTracker.decrementConnections();
			currentTask = null;
		}
	}
	
	private ReplyToClientTask computeHash(){
		if (debug) System.out.println("  COMPUTE HASH");
		HashComputer hashComp = new HashComputer();
		byte[] data = ((ComputeHashTask) currentTask).getBytes();
		String hashCode = hashComp.SHA1FromBytes(data);
		if (debug) System.out.println("Hashed " + data.length + " bytes: " + hashCode);
		currentTask.getKey().attach(null);
		ReplyToClientTask replyTask = new ReplyToClientTask(currentTask.getKey(), hashCode);
		currentTask = null;
		return replyTask;
	}
	
	private void reply() throws IOException{
		if (debug) System.out.println("  REPLY TO CLIENT");
		ByteBuffer buffer = ByteBuffer.allocate(40);
		if (debug) System.out.println("  Replying with hash: " + ((ReplyToClientTask) currentTask).getReplyHash());
		SelectionKey key = currentTask.getKey();
		key.attach(buffer);
		//System.out.println(key);
		byte[] data = new byte[40];
		data = ((ReplyToClientTask) currentTask).getReplyHash().getBytes();
		buffer.put(data);
		key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
		SocketChannel socketChannel = (SocketChannel) key.channel();
		int read = 0;
		buffer.rewind();
		try {
			while (buffer.hasRemaining() && read != -1){
				read = socketChannel.write(buffer);
			}
		} catch (NegativeArraySizeException e) {
			statTracker.decrementConnections();
			currentTask = null;
		}
		statTracker.incrementWrites();
		key.attach(null);
		currentTask = null;
	}
	
	public synchronized void assignTask(Task newTask){
		currentTask = newTask;
	}
	
	private void reportIdle(){
		if (debug) System.out.println("Worker thread " + workerThreadID + " reporting itself idle.");
		synchronized(idleThreads){
			idleThreads.add(this);
		}
		try {
			synchronized(this){
				this.wait();
			}
		} catch (InterruptedException e) {
			System.out.println(e);
		}
	}

}
