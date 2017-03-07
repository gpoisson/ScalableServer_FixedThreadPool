package cs455.nio_testing;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
    private static SocketChannel client;
    private static ByteBuffer buffer;
    private static Client instance;
 
    public static Client start() {
        if (instance == null)
            instance = new Client();
 
        return instance;
    }
 
    public static void stop() throws IOException {
        client.close();
        buffer = null;
    }
 
    private Client() {
        try {
            client = SocketChannel.open(new InetSocketAddress("localhost", 5454));
            buffer = ByteBuffer.allocate(256);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    public String sendMessage(String msg) {
        buffer = ByteBuffer.wrap(msg.getBytes());
        String response = null;
        System.out.println("Client beginning transmissions...");
        try {
        	System.out.println(" Client writing...");
            client.write(buffer);
            System.out.println(" Data written to buffer.");
            buffer.clear();
            System.out.println("  Buffer cleared.");
            client.read(buffer);
            System.out.println(" Data read from buffer.");
            response = new String(buffer.array()).trim();
            System.out.println("response=" + response);
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
 
    }
}