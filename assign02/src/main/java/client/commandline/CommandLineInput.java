package client.commandline;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.cli.CommandLine;

/**
 * Parses input options and stores information
 */
@Getter
@EqualsAndHashCode
@ToString
public final class CommandLineInput {
    private static final int MAX_THREADS = 512, MAX_SKIERS = 100000, MAX_LIFTS = 40, MAX_MEAN = 20,
            MIN_LIFTS = 5, DEFAULT_LIFTS = 10, DEFAULT_MEAN = 10;

    private final int numberThreads, numberSkiers, numberLifts, meanNumberLiftsPerSkier;
    private final String serverIPAddress;

    public CommandLineInput(CommandLine command) {
        try {
            //  maximum number of threads to run (numThreads - max 256)
            this.numberThreads = Integer.parseInt(command.getOptionValue("threads"));
            //  number of skier to generate lift rides for (numSkiers - max 100000), This is effectively the skier’s ID (skierID)
            this.numberSkiers = Integer.parseInt(command.getOptionValue("skiers"));
            //  number of ski lifts (numLifts - range 5-60, default 40)
            this.numberLifts = command.hasOption("lifts") ? Integer.parseInt(command.getOptionValue("lifts")) : DEFAULT_LIFTS;
            //  mean numbers of ski lifts each skier rides each day (numRuns - default 10, max 20)
            this.meanNumberLiftsPerSkier = command.hasOption("mean") ? Integer.parseInt(command.getOptionValue("mean")) : DEFAULT_MEAN;
            //  IP/port address of the server
            this.serverIPAddress = command.getOptionValue("ip");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unexpected parameter type encountered!");
        }
        if (!this.isValid()) {
            throw new IllegalArgumentException("Unexpected parameter value encountered!");
        }
    }

    private boolean isValid() {
        return this.numberThreads <= MAX_THREADS && this.numberSkiers <= MAX_SKIERS && this.numberLifts <= MAX_LIFTS && this.meanNumberLiftsPerSkier <= MAX_MEAN
                && this.numberLifts >= MIN_LIFTS && this.serverIPAddress != null && !this.serverIPAddress.isEmpty();
    }
}
