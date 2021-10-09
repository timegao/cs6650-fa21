package tcp.client;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketClientThread extends Thread {
    private long clientID;
    String hostName;
    int port;
    CyclicBarrier synk;

    public SocketClientThread(String hostName, int port, CyclicBarrier barrier) {
        this.hostName = hostName;
        this.port = port;
        clientID = Thread.currentThread().getId();
        synk = barrier;
    }

    public void run() {
        clientID = Thread.currentThread().getId();
        // TO DO insert code to pass messages to the SocketServer
        final int NUMBER_REQUESTS = 100;
        Socket s;
        for (int i = 0; i < NUMBER_REQUESTS; i++) {
            try {
                s = new Socket(hostName, port);
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                out.println("Client ID is " + Long.toString(clientID));

                System.out.println(in.readLine());

            } catch (UnknownHostException e) {
                // if we get an exception, don't bother retrying
                System.err.println("Don't know about host " + hostName);
                break;
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to " + hostName);
                // if we get an exception, don't bother retrying
                break;
            }
        } // end for
        try {
            // TO DO insert code to wait on the CyclicBarrier
            System.out.println("Thread waiting at barrier");
            synk.await();
        } catch (InterruptedException | BrokenBarrierException ex) {
            ex.printStackTrace();
        }

    }
}
