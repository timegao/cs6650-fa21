import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class HttpHelloClient {


    public static void main(String[] args) {
        final int NUM_THREADS = 100;
        final String URL = "http://127.0.0.1:8080/lab03/hello";

        CountDownLatch completed = new CountDownLatch(NUM_THREADS);

        System.out.println("Start time: " + System.currentTimeMillis());

        for (int i = 0; i < NUM_THREADS; i++) {
            // lambda runnable creation - interface only has a single method so lambda works fine
            Runnable thread = () -> {
                try {
                    // Create an instance of HttpClient.
                    CloseableHttpClient client = HttpClients.createDefault();
                    // Create a method instance.
                    HttpGet method = new HttpGet(URL);
                    client.execute(method);
                    System.out.println("Thread Id " + Thread.currentThread().getId() + ": " + System.currentTimeMillis());
                    method.releaseConnection();
                    completed.countDown();
                } catch (ClientProtocolException e) {
                    System.err.println("Fatal protocol violation: " + e.getMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    System.err.println("Fatal transport error: " + e.getMessage());
                    e.printStackTrace();
                }
            };
            new Thread(thread).start();
        }
    }
}