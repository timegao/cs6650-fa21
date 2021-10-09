package tcp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServerThreadPool {
    public static void main(String[] args) throws IOException {
        final int THREAD_POOL_SIZE = 128;

        // create socket listener
        ServerSocket listener = new ServerSocket(12031);

        ExecutorService service = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // create object to count active threads
        ActiveCount threadCount = new ActiveCount();
        System.out.println("Server started .....");
        while (true) {
            // accept connection and start thread
            Socket clientSocket = listener.accept();
            service.submit(new SocketHandlerThread(clientSocket, threadCount));
        }
    }
}
