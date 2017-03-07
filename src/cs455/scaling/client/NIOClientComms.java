package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOClientComms implements Runnable {

	SocketChannel socketChannel;
	private final String serverHostname;
	private final int serverPort;
	private final int messageRate;
	private final ByteBuffer buffer;
	private Selector selector;
	private boolean shutDown;
	private final boolean debug;
	
	public NIOClientComms(String serverHostname, int serverPort, int messageRate, boolean debug) throws IOException {
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		this.messageRate = messageRate;
		this.shutDown = false;
		this.buffer = ByteBuffer.allocate(60);
		this.debug = debug;
		selector = Selector.open();
		socketChannel = SocketChannel.open();
	}
	
	@Override
	public void run() {
		try {
			startClient();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	private void startClient() throws IOException {
		if (debug) System.out.println("NIOClientComms starting the client...");
		//SelectionKey key = socketChannel.register(selector, SelectionKey.OP_CONNECT);
		//connect(key);
		socketChannel.connect(new InetSocketAddress(serverHostname, serverPort));
		if (debug) System.out.println("Client connected to server: " + socketChannel.getRemoteAddress());
		while (!shutDown){
			socketChannel.write(buffer);
			buffer.clear();
			socketChannel.read(buffer);
			String receivedHash = new String(buffer.array());
			if (debug) System.out.println(" Client received msg from server: " + receivedHash);
			buffer.clear();
			long waitTime = (long) (1.0/messageRate);
			if (debug) System.out.println(" Client waiting for " + waitTime + " seconds...");
			
		}

	}
	
	private void connect (SelectionKey key) throws IOException {
		socketChannel.connect(new InetSocketAddress(serverHostname, serverPort));
		if (debug) System.out.println("  Client connect() finishing connect; setting interest to WRITE");
		SocketChannel channel = (SocketChannel) key.channel();
		channel.finishConnect();
		key.interestOps(SelectionKey.OP_WRITE);
	}
}
