package cs455.nio_testing;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {
 
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", 5454));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer buffer = ByteBuffer.allocate(256);
 
        while (true) {
        	System.out.println("Server waiting for selector.select()");
            selector.select();
            Set selectedKeys = selector.selectedKeys();
            Iterator iter = selectedKeys.iterator();
            while (iter.hasNext()) {
            	System.out.println("Current key available");
                SelectionKey key = (SelectionKey) iter.next();
 
                if (key.isAcceptable()) {
                	System.out.println(" Key acceptable");
                    SocketChannel client = serverSocket.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                }
 
                if (key.isReadable()) {
                	System.out.println(" Key readable");
                    SocketChannel client = (SocketChannel) key.channel();
                    client.read(buffer);
                    buffer.flip();
                    client.write(buffer);
                    buffer.clear();
                }
                System.out.println(" Key removed");
                iter.remove();
            }
        }
    }
 
    public static Process start() throws IOException, InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = Server.class.getCanonicalName();
 
        ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className);
 
		return builder.start();
    }
}