package main.part1;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import main.part1.commandline.CommandLineInput;
import main.part1.commandline.CommandLineOptions;
import main.part1.task.ExecutorServiceTask;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Client parses the commandline input and calls the ExecutorServiceTask to run
 */
public final class MultiThreadedClient {

    private static final int RESORT_ID = 123;
    private static final String SEASON_ID = "456", DAY_ID = "789";


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

//        int localSuccesses = 0;
//        int localFailures = 0;
//        long startTime = System.currentTimeMillis();
//
//        ApiClient client = new ApiClient().setConnectTimeout(60000).setReadTimeout(60000);
//        client.setBasePath("http://34.205.18.204:8080/lab02_war");
//        SkiersApi apiInstance = new SkiersApi(client);
//        for (int i = 0; i < 10000; i++) {
//            int skierID = (int) Math.ceil(Math.random() * 60) - 5;
//            LiftRide liftRide = new LiftRide();
//            liftRide.setLiftID((int) Math.ceil(Math.random() * 40));
//            liftRide.setTime((int) Math.ceil(Math.random() * 420 - 1));
//
//            try {
//                apiInstance.writeNewLiftRide(liftRide, RESORT_ID, SEASON_ID, DAY_ID, skierID);
//                localSuccesses++;
//            } catch (ApiException e) {
//                e.printStackTrace();
//                localFailures++;
//            }
//        }
//
//        long endTime = System.currentTimeMillis();
//        System.out.println("thread(s): 1");
//        System.out.println("successes: " + localSuccesses);
//        System.out.println("failures: " + localFailures);
//        System.out.println("wall: " + (endTime - startTime));
    }
}
