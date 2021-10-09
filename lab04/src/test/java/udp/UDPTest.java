package udp;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import udp.client.EchoClient;
import udp.server.EchoServer;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

public class UDPTest {
    EchoClient client;

    @Before
    public void setup() throws SocketException, UnknownHostException {
        new EchoServer().start();
        client = new EchoClient();
    }

    @Test
    public void whenCanSendAndReceivePacket_thenCorrect() throws IOException {
        String echo = client.sendEcho("hello server");
        Assert.assertEquals("hello server", echo);
        echo = client.sendEcho("server is working");
        Assert.assertEquals("server is working", echo);
    }

    @Test
    public void whenSendReceiveThousandPacket_thenCorrect() throws IOException, InterruptedException {
        final int NUMBER_THREADS = 50, NUMBER_REQUESTS = 1000;
        CountDownLatch count = new CountDownLatch(NUMBER_THREADS);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < NUMBER_THREADS; i++) {
            EchoClient echoClient = new EchoClient();
            for (int j = 0; j < NUMBER_REQUESTS; j++) {
                int finalJ = j;
                int finalI = i;
                Runnable thread = () -> {
                    try {
                        echoClient.sendEcho(finalI + "" + finalJ);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
                thread.run();
            }
            count.countDown();
        }
        long endTime = System.currentTimeMillis();

        System.out.println("wall time: " + (endTime - startTime));
        count.await();
    }

    @After
    public void tearDown() throws IOException {
        client.sendEcho("end");
        client.close();
    }
}