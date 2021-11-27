package consumer;

import consumer.commandline.ConsumerCommandLineInput;
import consumer.commandline.ConsumerCommandLineOptions;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;

public class QueueConsumer {
    public static void main(String[] args) {
        Options instance = ConsumerCommandLineOptions.getInstance();
        ConsumerCommandLineInput input = null;

        try {
            input = new ConsumerCommandLineInput(new DefaultParser().parse(instance, args));
        } catch (ParseException | IllegalArgumentException e) {
            e.printStackTrace();
            new HelpFormatter().printHelp("options", instance);
            System.exit(1);
        }

        BasicConfigurator.configure();

        for (int i = 0; i < input.getNumberThreads(); i++) {
            new Thread(new ReadQueueWriteDatabaseTask(input.getQueueName())).start();
        }
    }
}
