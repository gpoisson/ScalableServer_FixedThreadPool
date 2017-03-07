package cs455.scaling.client;

import java.io.IOException;
import java.util.LinkedList;

import cs455.scaling.Node;
import cs455.util.HashComputer;

public class Client implements Node {

	private String serverHost;						// Server IP address
	private int serverPort;							// Server port number
	private int messageRate;						// Number of message to send per second
	
	private HashComputer hashComputer;				// Takes a byte array as input and returns an integer hash value using the SHA1 algorithm
	private LinkedList<String> hashCodes;			// A queue of hash codes for messages sent by the client
	
	private Thread comm;							// Client communications thread
	
	private Client () {
		hashComputer = new HashComputer();
		hashCodes = new LinkedList<String>();
	}
	
	public static void main(String[] args) throws IOException {
		
		Client client = new Client();
		
		// Parse command arguments
		if (args.length == 3) {
			client.serverHost = args[0];
			client.serverPort = Integer.parseInt(args[1]);
			client.messageRate = Integer.parseInt(args[2]);
		}
		else {
			System.out.println(usage());
			System.exit(0);
		}
		
		System.out.println("New client initialized.  Server host: " + client.serverHost + " \tServer Port: " + client.serverPort + "\tMessageRate: " + client.messageRate);
	
		//client.comm = new Thread(new ClientComms(client.serverHost, client.serverPort, client.messageRate, debug));
		client.comm = new Thread(new NIOClientComms(client.serverHost, client.serverPort, client.messageRate, debug));
		client.comm.start();
	}
	
	public static String usage() {
		return "Usage:  Client <server-host> <server-port> <message-rate>";
	}
}
