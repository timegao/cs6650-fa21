package client.commandline;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Initializes the options based on the specifications.
 */
public final class CommandLineOptions {

    private static Options instance;

    private static void initializeOptions() {
        instance = new Options();
        Option threads = new Option("t", "threads", true, "number of threads, max 512");
        threads.setRequired(true);
        Option skiers = new Option("s", "skiers", true, "number of skier IDs, max 100,000");
        skiers.setRequired(true);
        Option lifts = new Option("l", "lifts", true, "number of ski lifts, range 5-60, default 40");
        Option mean = new Option("m", "mean", true, "mean number ski lifts per ski rider per day, default 10, max 20");
        Option ip = new Option("i", "ip", true, "ip address of server");
        ip.setRequired(true);

        instance.addOption(threads);
        instance.addOption(skiers);
        instance.addOption(lifts);
        instance.addOption(mean);
        instance.addOption(ip);
    }

    public synchronized static Options getInstance() {
        if (instance == null) {
            initializeOptions();
        }
        return instance;
    }
}
