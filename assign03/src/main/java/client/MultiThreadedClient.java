package client;

import client.commandline.ClientCommandLineInput;
import client.commandline.ClientCommandLineOptions;
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

        Options instance = ClientCommandLineOptions.getInstance();
        ClientCommandLineInput input = null;

        try {
            input = new ClientCommandLineInput(new DefaultParser().parse(instance, args));
        } catch (ParseException | IllegalArgumentException e) {
            e.printStackTrace();
            new HelpFormatter().printHelp("options", instance);
            System.exit(1);
        }

        ExecutorServiceTask executorServiceTask = new ExecutorServiceTask(input);
        executorServiceTask.run();
    }
}
