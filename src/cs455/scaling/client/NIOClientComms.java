package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOClientComms implements Runnable {

	SocketChannel socketChannel;
	Selector selector;
	private final String serverHostname;
	private final int serverPort;
	private final int messageRate;
	private SelectionKey key;
	private boolean shutDown;
	private final boolean debug;
	
	public NIOClientComms(String serverHostname, int serverPort, int messageRate, boolean debug) {
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		this.messageRate = messageRate;
		this.shutDown = false;
		this.debug = debug;
		try {
			//SocketAddress address = new InetSocketAddress(this.serverHostname, this.serverPort);
			//socketChannel = SocketChannel.open(address);
			socketChannel = SocketChannel.open();
			selector = Selector.open();
		} catch (IOException e) {
			System.out.println(e);
		}
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
		socketChannel.configureBlocking(false);
		key = socketChannel.register(selector, SelectionKey.OP_CONNECT);
		socketChannel.connect(new InetSocketAddress(serverHostname, serverPort));
		if (debug) System.out.println("  Client connected to server.");
		while (!shutDown) {
			if (key.isConnectable()) {
				connect(key);
				if (debug) System.out.println(" Client connected");
			}
			else if (key.isAcceptable()) {
				if (debug) System.out.println(" Key is acceptable");
			}
			else if (key.isWritable()) {
				if (debug) System.out.println(" Client ready to write to channel");
			}
			else if (key.isReadable()) {
				if (debug) System.out.println(" Client ready to read from channel");
			}
			else {
				//if (debug) System.out.println(" Client key: " + key.interestOps());
			}
			//System.out.println(SelectionKey.OP_ACCEPT + " " + SelectionKey.OP_CONNECT + " " + SelectionKey.OP_READ + " " + SelectionKey.OP_WRITE);
		}
	}
	
	private void connect (SelectionKey key) throws IOException {
		if (debug) System.out.println("  Client connect() finishing connect; setting interest to WRITE");
		SocketChannel channel = (SocketChannel) key.channel();
		channel.finishConnect();
		key.interestOps(SelectionKey.OP_WRITE);
	}
}
