package cs455.scaling;

public class Client implements Node {

	private String serverHost;
	private int serverPort;
	private int messageRate;
	
	private Thread comm;
	
	public Client () {
		
	}
	
	public static void main(String[] args) {
		
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
	
		client.comm = new Thread(new ClientComms(client.serverHost, client.serverPort, client.debug));
		client.comm.start();
	}
	
	public static String usage() {
		return "Usage:  Client <server-host> <server-port> <message-rate>";
	}
}
