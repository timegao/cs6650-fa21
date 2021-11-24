package consumer.commandline;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class ConsumerCommandLineOptions {

    private static Options instance;

    private static void initializeOptions() {
        instance = new Options();
        Option threads = new Option("t", "threads", true, "number of threads");
        threads.setRequired(true);
        Option queue = new Option("q", "queueName", true, "name of the queue");
        queue.setRequired(true);
        instance.addOption(threads);
        instance.addOption(queue);
    }

    public synchronized static Options getInstance() {
        if (instance == null) {
            initializeOptions();
        }
        return instance;
    }
}