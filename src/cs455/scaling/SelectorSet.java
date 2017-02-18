package cs455.scaling;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class SelectorSet {
	
	private ArrayList<Selector> selectors;
	private int channelsPerSelector;
	private boolean debug;

	public SelectorSet(int channelsPerSelector, boolean debug) {
		this.debug = debug;
		this.channelsPerSelector = channelsPerSelector;
		selectors = new ArrayList<Selector>();
	}
	
	public synchronized void addNewSelector() {
		try {
			if (debug) System.out.println("  Creating a new selector...");
			Selector selector = Selector.open();
			selectors.add(selector);
			if (debug) System.out.println("  New selector created; ready to register channels.");
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	// 
	public synchronized void registerChannel(SocketChannel channel) {
		for (Selector selector: selectors) {
			if (selector.keys().size() < channelsPerSelector) {
				try {
					if (debug) System.out.println("  Registering channel to selector...");
					channel.configureBlocking(false);
					SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
					if (debug) System.out.println("  Channel registered.");
				} catch (IOException e) {
					System.out.println(e);
				}
			}
		}
	}
}
