package tcp.server;

import java.io.*;
import java.net.Socket;

class SocketHandlerThread extends Thread {
    private final Socket conn;
    private final ActiveCount threadCount;

    SocketHandlerThread(Socket s, ActiveCount threads) {
        conn = s;
        threadCount = threads;
    }

    public void run() {
        threadCount.incrementCount();
        System.out.println("Accepted Client: Address - " + conn.getInetAddress().getHostName());
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()));

            String clientID = in.readLine();
            // Uncomment to see what tcp.client sent
            System.out.println(clientID);
            out.println("Active Server Thread Count = " + threadCount.getCount());
            out.flush();
            // uncomment to ensure reply sent
            System.out.println("Reply sent");

        } catch (IOException e) {
            System.out.println("Exception in thread");
        } finally {
            try {
                conn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            threadCount.decrementCount();
            System.out.println("Thread exiting");
        }
    }

} // end class