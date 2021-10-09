package tcp.client;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class SocketClientMultiThreaded {

    static CyclicBarrier barrier;

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        String hostName;
        final int MAX_THREADS = 50;
        int port;

        if (args.length == 2) {
            hostName = args[0];
            port = Integer.parseInt(args[1]);
        } else {
            hostName = null;
            port = 12031; // default port in SocketServer
        }
        // TO DO finalize the initialization of barrier below
        barrier = new CyclicBarrier(MAX_THREADS + 1);

        long startTime = System.currentTimeMillis();
        // TO DO create and start MAX_THREADS SocketClientThread
        for (int i = 0; i < MAX_THREADS; i++) {
            new SocketClientThread(hostName, port, barrier).start();
        }

        // TO DO wait for all threads to complete
        barrier.await();
        long endTime = System.currentTimeMillis();
        System.out.println("Wall time: " + (endTime - startTime));

        System.out.println("Terminating ....");
    }

}