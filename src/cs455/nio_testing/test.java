package cs455.nio_testing;

import java.io.IOException;

public class test {
	 
    Process server;
    Client client;
 
    @Before
    public void setup() throws IOException, InterruptedException {
        server = Server.start();
        client = Client.start();
    }
 
    @Test
    public void givenServerClient_whenServerEchosMessage_thenCorrect() {
        String resp1 = client.sendMessage("hello");
        String resp2 = client.sendMessage("world");
        assertEquals("hello", resp1);
        assertEquals("world", resp2);
    }
 
    @After
    public void teardown() throws IOException {
        server.destroy();
        Client.stop();
    }
}