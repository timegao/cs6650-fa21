package main.part2.task;

import main.part2.commandline.CommandLineInput;
import main.part2.commandline.ResponseAnalyzer;
import main.part2.model.Lift;
import main.part2.model.Request;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.*;

/**
 * Executes the jobs put into ExecutorService
 */
public final class ExecutorServiceTask implements Runnable {
    private static final int STARTUP_COOLDOWN_THREAD_FACTOR = 4, PEAK_THREAD_FACTOR = 1, STARTUP_START_TIME = 1,
            STARTUP_END_TIME = 9, PEAK_START_TIME = 91, PEAK_END_TIME = 360, COOLDOWN_START_TIME = 361, COOLDOWN_END_TIME = 420;
    private static final double THREAD_POOL_MULTIPLIER = 2, NEXT_PHASE_FACTOR = 0.1, FINAL_PHASE_FACTOR = 0.25,
            STARTUP_RUN_FACTOR = 0.2, PEAK_RUN_FACTOR = 0.6, COOLDOWN_RUN_FACTOR = 0.1;

    private final int numberThreads, numberSkiers, meanNumberLiftsPerSkier;
    private final CommandLineInput input;

    private final ConcurrentLinkedQueue<Request> successQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Request> failureQueue = new ConcurrentLinkedQueue<>();

    public ExecutorServiceTask(CommandLineInput input) {
        this.numberThreads = input.getNumberThreads();
        this.numberSkiers = input.getNumberSkiers();
        this.meanNumberLiftsPerSkier = input.getMeanNumberLiftsPerSkier();
        this.input = input;
    }

    @Override
    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool((int) Math.round(THREAD_POOL_MULTIPLIER * this.numberThreads));
        long startTime = System.currentTimeMillis();

        try {
            // startup phase
            startPhase(executor, STARTUP_START_TIME, STARTUP_END_TIME, STARTUP_COOLDOWN_THREAD_FACTOR,
                    NEXT_PHASE_FACTOR, STARTUP_RUN_FACTOR);
            // peak phase
            startPhase(executor, PEAK_START_TIME, PEAK_END_TIME, PEAK_THREAD_FACTOR, NEXT_PHASE_FACTOR,
                    PEAK_RUN_FACTOR);
            // cooldown phase
            startPhase(executor, COOLDOWN_START_TIME, COOLDOWN_END_TIME, STARTUP_COOLDOWN_THREAD_FACTOR,
                    FINAL_PHASE_FACTOR, COOLDOWN_RUN_FACTOR);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        ResponseAnalyzer analyzer = new ResponseAnalyzer(this.successQueue);
        System.out.println("thread(s): " + this.numberThreads);
        System.out.println("successes: " + this.successQueue.size());
        System.out.println("failures: " + this.failureQueue.size());
        System.out.println("wall: " + (endTime - startTime));
        System.out.println("throughput: " + (float) this.successQueue.size() / (endTime - startTime));
        System.out.println("mean: " + analyzer.getMean());
        System.out.println("median: " + analyzer.getMedian());
        System.out.println("max: " + analyzer.getMax());
        System.out.println("p99: " + analyzer.getP99());


        try {
            FileWriter writer = new FileWriter(this.numberThreads + ".csv");
            writeCSVFile(writer, this.successQueue);
            writeCSVFile(writer, this.failureQueue);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startPhase(ExecutorService executor, int startMinute, int endMinute, int threadFactor,
                            double nextPhaseFactor, double runsFactor) throws InterruptedException {
        CountDownLatch completed = new CountDownLatch((int) Math.ceil(this.numberThreads * nextPhaseFactor));
        int numberRequests = (int) Math.floor(this.meanNumberLiftsPerSkier * runsFactor *
                this.numberSkiers * threadFactor / numberThreads);

        for (int i = 0; i < this.numberThreads / threadFactor; i++) {
            int startIDNumber = i * threadFactor * this.numberSkiers / this.numberThreads + 1;
            int endIDNumber = (i + 1) * threadFactor * this.numberSkiers / this.numberThreads;
            Lift lift = new Lift(startIDNumber, endIDNumber, startMinute, endMinute);
            Runnable thread = () -> {
                executor.execute(new PostRequestTask(this.input, this.successQueue,
                        this.failureQueue, lift, numberRequests));
                completed.countDown();
            };
            new Thread(thread).start();
        }
        completed.await();
    }

    private void writeCSVFile(FileWriter writer, ConcurrentLinkedQueue<Request> queue) throws IOException {
        queue.forEach(r -> {
            try {
                writer.write(r.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
