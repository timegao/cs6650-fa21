package consumer.commandline;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class ConsumerCommandLineOptions {

    private static Options instance;

    private static void initializeOptions() {
        instance = new Options();
        Option threads = new Option("t", "threads", true, "number of threads");
        threads.setRequired(true);
        Option skiers = new Option("s", "skiers", true, "number of skier IDs, default 20,000, max 100,000");
        Option host = new Option("h", "host", true, "host name of connection, default localhost");
        Option queue = new Option("q", "queue", true, "queue name of channel, default 'test'");
        Option username = new Option("u", "username", true, "username of the connection, default 'guest'");
        Option password = new Option("p", "password", true, "password of the connection, default 'guest'");

        instance.addOption(threads);
        instance.addOption(skiers);
        instance.addOption(host);
        instance.addOption(queue);
        instance.addOption(username);
        instance.addOption(password);
    }

    public synchronized static Options getInstance() {
        if (instance == null) {
            initializeOptions();
        }
        return instance;
    }
}
