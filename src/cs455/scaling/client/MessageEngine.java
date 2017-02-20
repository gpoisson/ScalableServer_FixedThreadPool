package cs455.scaling.client;

import java.nio.channels.SocketChannel;

import cs455.message.HashMessage;

public class MessageEngine implements Runnable {

	private SocketChannel socketChannel;
	private int messageRate;
	private boolean shutDown = false;
	private boolean debug;
	
	public MessageEngine(SocketChannel socketChannel, int messageRate, boolean debug) {
		this.messageRate = messageRate;
		this.debug = debug;
	}
	
	@Override
	public void run() {
		if (debug) System.out.println("  Message engine running...");
		while (!shutDown) {
			if (debug) System.out.println("   Message engine generating new hash message...");
			HashMessage hashMsg = new HashMessage();
		}
	}
	
	private void pause(int seconds) {
		try {
			Thread.sleep(seconds);
		} catch (InterruptedException e) {
			System.out.println(e);
		}
	}

}
