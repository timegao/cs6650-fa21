package consumer.commandline;

import lombok.Data;
import org.apache.commons.cli.CommandLine;

@Data
public class ConsumerCommandLineInput {

    private static final int MAX_SKIERS = 100000, DEFAULT_SKIER_NUMBER = 20000;
    private static final String DEFAULT_HOST_NAME = "localhost", DEFAULT_QUEUE_NAME = "test",
            DEFAULT_USERNAME = "guest", DEFAULT_PASSWORD = "guest";

    private final int numberThreads, numberSkiers;
    private final String hostName, queueName, username, password;

    public ConsumerCommandLineInput(CommandLine command) {
        try {
            this.numberThreads = Integer.parseInt(command.getOptionValue("threads"));
            this.numberSkiers = command.hasOption("skiers") ? Integer.parseInt(command.getOptionValue("skiers")) : DEFAULT_SKIER_NUMBER;
            this.hostName = command.hasOption("host") ? command.getOptionValue("host") : DEFAULT_HOST_NAME;
            this.queueName = command.hasOption("queue") ? command.getOptionValue("queue") : DEFAULT_QUEUE_NAME;
            this.username = command.hasOption("username") ? command.getOptionValue("username") : DEFAULT_USERNAME;
            this.password = command.hasOption("password") ? command.getOptionValue("password") : DEFAULT_PASSWORD;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unexpected parameter type encountered!");
        }
        if (!this.isValid()) {
            throw new IllegalArgumentException("Unexpected parameter value encountered!");
        }
    }

    private boolean isValid() {
        return this.numberSkiers <= MAX_SKIERS && this.hostName != null && this.queueName != null;
    }
}
