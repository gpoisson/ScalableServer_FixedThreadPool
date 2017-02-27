package cs455.scaling.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import cs455.scaling.server.tasks.Task;

public class WorkerThread implements Runnable {

	private final int workerThreadID;
	private Task currentTask;
	private LinkedList<WorkerThread> idleThreads;
	private int numConnections;
	private Selector selector;
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
		this.numConnections = 0;

		try {
			this.selector = Selector.open();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	@Override
	public void run() {
		if (debug) System.out.println("  New worker thread " + workerThreadID + " executed.");
		while (!shutDown) {
			if (currentTask != null) {
				if (debug) System.out.println("  Worker thread " + workerThreadID + " has a task.");
			}
			else {
				//reportIdle();
				try {
					if (debug) System.out.println(" Worker thread " + workerThreadID + " waiting for incoming data.");
					this.selector.select();
					
					Iterator keys =	this.selector.selectedKeys().iterator();   
					
					while(keys.hasNext()) {
						SelectionKey key = (SelectionKey) keys.next();
						if (key.isAcceptable())   
						{   
							this.accept(key);
						}  
					}
					if (debug) System.out.println(" Worker thread " + workerThreadID + " received incoming data.");
				} catch (IOException e) {
					System.out.println(e);
				}
			}
		}
	}
	
	private void accept(SelectionKey key) throws IOException {
		ServerSocketChannel	servSocket = (ServerSocketChannel) key.channel();   
		SocketChannel channel = servSocket.accept();
		System.out.println("Accepting incoming connection");   
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ);   
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
	
	public int getConnectionCount() {
		return this.numConnections;
	}
	
	public int getId() {
		return this.workerThreadID;
	}
	
	public synchronized void registerNewSocketChannel(SocketChannel socketChannel) {
		try {
			if (debug) System.out.println("Worker thread " + workerThreadID + " accepting new socket channel from TPM...");
			socketChannel.configureBlocking(false);
			SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
			if (debug) System.out.println("Worker thread " + workerThreadID + " registered new socket channel.");
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public synchronized void assignTask(Task newTask) {
		if (debug) System.out.println("Worker thread " + workerThreadID + " accepting new task...");
		if (newTask != null) {
			idle = false;
			currentTask = newTask;
		}
	}
}
