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

	private final int workerThreadID;					// Unique ID number used for tracking and debugging
	private Task currentTask;							// This worker thread's current task, if any
	private LinkedList<WorkerThread> idleThreads;		// Reference to thread pool manager's list of idle threads
	private ReplyToClientTask replyTask;				// Reply task to be handed back to thread pool manager once read and hash tasks have been completed
	private boolean debug;								// Debug mode
	private final int readBufferSize;					// Size of read buffer
	private final StatTracker statTracker;				// Reference to server stat tracker
	private boolean shutDown;							// Shut down switch
	private boolean idle;								// Idle flag
	
	public final Object sleepLock;
	
	public WorkerThread(LinkedList<WorkerThread> idleThreads, int id, StatTracker statTracker, boolean debug) {
		this.debug = debug; 
		this.workerThreadID = id;
		this.shutDown = false;
		this.idleThreads = idleThreads;
		this.currentTask = null;
		this.readBufferSize = 8192;
		this.replyTask = null;
		this.statTracker = statTracker;
		this.sleepLock = new Object();
	}
	
	@Override
	public void run() {
		if (debug) System.out.println("  New worker thread " + workerThreadID + " executed.");
		
		while (!shutDown) {
			// If this thread has been given a task, perform the task
			if (currentTask != null) {
				if (debug) System.out.println("  Worker thread " + workerThreadID + " has a task.");
				SelectionKey key = currentTask.getKey();
				String result = null;
				try {
					result = processTask();				// processTask returns a computed hash if task is a read task; null if task is a write task
				} catch (NegativeArraySizeException e) {
					statTracker.decrementConnections();
					currentTask = null;
				}
				if (result != null) {						// If task was a read task, hand the thread pool manager a reply task
					replyTask = new ReplyToClientTask(key, result);
					currentTask = null;
				}
				
			}
			else {
				reportIdle();								// If there is no current task, report idle to thread pool manager
			}
			if (debug) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.out.println(e);
				}
			}
		}
	}
	
	// Perform a task if there is one to do
	private String processTask() {
		// Read incoming data from client
		if (currentTask instanceof AcceptIncomingTrafficTask) {
			ByteBuffer buffer = ByteBuffer.allocate(readBufferSize);
			if (debug) System.out.println("Worker thread " + workerThreadID + " reading data from channel...");
			
			SelectionKey key = currentTask.getKey();
			synchronized (key) {
				SocketChannel clientChannel = (SocketChannel) key.channel();
				int read = 0;
				key.attach(buffer);
				// Read data from channel into buffer
				try {
					while (buffer.hasRemaining() && read != -1) {
						read = clientChannel.read(buffer);
					}
					buffer.rewind();
					
					// Load data from buffer into byte array
					byte[] payloadBytes = new byte[read];
					for (int i = 0; i < read; i++){
						payloadBytes[i] = buffer.get();
					}
					buffer.clear();
					buffer = null;
					
					// Prepare to compute the hash value for the received payload
					this.statTracker.incrementReads();
					ComputeHashTask computeHashTask = new ComputeHashTask(key, payloadBytes);
					currentTask = computeHashTask;
				} catch (IOException e) {
					// Abnormal termination
					/*  server.disconnect(key);
					 *  return;
					*/
					System.out.println(e);
					System.out.println("Abnormal termination: case A. Removing this key.");
					key.cancel();
				}
				if (read == -1) {
					statTracker.decrementConnections();
					// Connection terminated by client
					/*	server.disconnect(key);
					 *  return;
					 */
					System.out.println("Connection terminated by client. Removing this key.");
					key.cancel();
				}
				//buffer.clear();
			}
		}
		// Compute hash of received payload
		if (currentTask instanceof ComputeHashTask) {
			if (debug) System.out.println("Worker thread " + workerThreadID + " computing hash of byte array...");
			HashComputer hashComputer = new HashComputer();
			String sha = hashComputer.SHA1FromBytes(((ComputeHashTask) currentTask).getBytes());
			if (debug) System.out.println("Worker thread " + workerThreadID + " computed hash: " + sha);
			statTracker.incrementHashes();
			return sha;
		}
		// Reply to client with computed hash string
		else if (currentTask instanceof ReplyToClientTask) {
			ByteBuffer buffer = ByteBuffer.allocate(((ReplyToClientTask) currentTask).getReplyHash().getBytes().length);
			if (debug) System.out.println("Worker thread " + workerThreadID + " writing data to channel...");
			SelectionKey key = currentTask.getKey();
			synchronized (key) {
				SocketChannel clientChannel = (SocketChannel) key.channel();
				int read = 0;
				buffer.rewind();
				buffer.put(((ReplyToClientTask) currentTask).getReplyHash().getBytes());
				buffer.rewind();
				try {
					while (buffer.hasRemaining() && read != -1) {
						read = clientChannel.write(buffer);
		                //buffer.flip();
					}
					currentTask.getKey().attach(null);
					if (debug) System.out.println("...Data written to channel.  read: " + read);
					
					if (debug) {
						while (buffer.hasRemaining()){
							System.out.print((char) buffer.get());
						}
					}
					buffer.clear();
					if (debug) System.out.println();
					statTracker.incrementWrites();
					buffer.flip();
				} catch (IOException e) {
					// Abnormal termination
					statTracker.decrementConnections();
					/*
					 *  ADD SERVER DISCONNECT HERE
					 *  server.disconnect(key);
					 *  return;
					 */
					System.out.println(e);
					System.out.println("Abnormal termination: case B. Removing this key.");
					key.cancel();
				}
				// Data stored in 'data' of type: byte[]
				/*ByteBuffer buffer = ByteBuffer.wrap(data);
				 * channel.write(buffer);
				 * key.interestOps(SelectionKey.OP_READ);
				 */
				//if (debug) System.out.println(" Switching key interest to READ");
				//key.interestOps(SelectionKey.OP_READ);
			}
			currentTask = null;
		}
		return null;
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
					if (debug) System.out.println("  Worker thread " + workerThreadID + " is idle and is going to sleep.");
					sleepLock.wait();
					if (debug) System.out.println("  Worker thread " + workerThreadID + " has been woken up by the thread pool manager.");
				} catch (InterruptedException e) {
					System.out.println(e);
				}
			}
		}
	}
	
	public ReplyToClientTask extractPendingReplyTask() {
		ReplyToClientTask newTask = new ReplyToClientTask(null, null);
		if (replyTask == null) return null;
		synchronized(replyTask) {
			newTask = replyTask;
		}
		replyTask = null;
		return newTask;
	}
	
	public boolean isIdle() {
		if (idle) return true;
		else return false;
	}
	
	public int getId() {
		return this.workerThreadID;
	}
	
	/*public synchronized void registerNewSocketChannel(SocketChannel socketChannel) {
		try {
			if (debug) System.out.println("Worker thread " + workerThreadID + " accepting new socket channel from TPM...");
			socketChannel.configureBlocking(false);
			SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
			if (debug) System.out.println("Worker thread " + workerThreadID + " registered new socket channel.");
		} catch (IOException e) {
			System.out.println(e);
		}
	}*/
	
	public synchronized void assignTask(Task newTask) {
		if (debug) System.out.println("Worker thread " + workerThreadID + " accepting new task...");
		//synchronized (currentTask) {
			if (newTask != null) {
				idle = false;
				currentTask = newTask;
				if (debug) System.out.println("Worker thread " + workerThreadID + " reports new task accepted.");
			}
			else {
				if (debug) System.out.println("Worker thread " + workerThreadID + " reports there is already a current task!!!");
			}
		//}
	}
}
