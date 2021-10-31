package client;

import client.commandline.CommandLineInput;
import client.commandline.CommandLineOptions;
import client.task.ExecutorServiceTask;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Client parses the commandline input and calls the ExecutorServiceTask to run
 */
public final class MultiThreadedClient {

    public static void main(String[] args) {

        Options instance = CommandLineOptions.getInstance();
        CommandLineInput input = null;

        try {
            input = new CommandLineInput(new DefaultParser().parse(instance, args));
        } catch (ParseException | IllegalArgumentException e) {
            e.printStackTrace();
            new HelpFormatter().printHelp("options", instance);
            System.exit(1);
        }

        ExecutorServiceTask executorServiceTask = new ExecutorServiceTask(input);
        executorServiceTask.run();
    }
}
