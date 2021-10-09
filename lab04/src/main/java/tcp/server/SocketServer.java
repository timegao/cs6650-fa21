package tcp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    public static void main(String[] args) throws IOException {
        // create socket listener
        ServerSocket listener = new ServerSocket(12031);
        // create object to count active threads
        ActiveCount threadCount = new ActiveCount();
        System.out.println("Server started .....");
        while (true) {
            // accept connection and start thread
            Socket clientSocket = listener.accept();
            SocketHandlerThread server = new SocketHandlerThread(clientSocket, threadCount);
            server.start();
        }
    }
}