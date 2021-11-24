package consumer.commandline;

import lombok.Data;
import org.apache.commons.cli.CommandLine;

@Data
public class ConsumerCommandLineInput {

    private final int numberThreads;
    private final String queueName;

    public ConsumerCommandLineInput(CommandLine command) {
        try {
            this.numberThreads = Integer.parseInt(command.getOptionValue("threads"));
            this.queueName = command.getOptionValue("queueName");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unexpected parameter type encountered!");
        }
    }
}
