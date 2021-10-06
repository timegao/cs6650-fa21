package main.part1.task;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import lombok.RequiredArgsConstructor;
import main.part1.commandline.CommandLineInput;
import main.part1.model.Counter;
import main.part1.model.Lift;

/**
 * Each request creates numberRequest of post requests and random LiftRides
 */
@RequiredArgsConstructor
public final class PostRequestTask implements Runnable {
    private static final int RESORT_ID = 123;
    private static final String SEASON_ID = "456", DAY_ID = "789";

    private final int numberLifts, numberRequests;
    private final String serverIPAddress;
    private final Lift lift;
    private final Counter successCount, failureCount;

    public PostRequestTask(CommandLineInput input, Counter successCount, Counter failureCount, Lift lift, int numberRequests) {
        this.serverIPAddress = input.getServerIPAddress();
        this.numberLifts = input.getNumberLifts();
        this.lift = lift;
        this.numberRequests = numberRequests;
        this.successCount = successCount;
        this.failureCount = failureCount;
    }

    @Override
    public void run() {
        ApiClient client = new ApiClient().setConnectTimeout(60000).setReadTimeout(60000);
        client.setBasePath(this.serverIPAddress);
        SkiersApi apiInstance = new SkiersApi(client);

        for (int i = 0; i < this.numberRequests; i++) {
            int skierID = (int) Math.ceil(Math.random() * lift.getEndIDNumber()) - lift.getStartIDNumber();
            LiftRide liftRide = createRandomLiftRide();
            createPostRequest(apiInstance, liftRide, skierID);
        }
    }

    private LiftRide createRandomLiftRide() {
        LiftRide liftRide = new LiftRide();
        liftRide.setLiftID((int) Math.ceil(Math.random() * this.numberLifts));
        liftRide.setTime((int) Math.ceil(Math.random() * this.lift.getEndTime() - this.lift.getStartTime()));
        return liftRide;
    }

    private void createPostRequest(SkiersApi apiInstance, LiftRide liftRide, int skierID) {
        int localSuccesses = 0;
        int localFailures = 0;
        try {
            apiInstance.writeNewLiftRide(liftRide, RESORT_ID, SEASON_ID, DAY_ID, skierID);
            localSuccesses++;
        } catch (ApiException e) {
            e.printStackTrace();
            localSuccesses++;
        }
        this.successCount.increaseBy(localSuccesses);
        this.failureCount.increaseBy(localFailures);
    }
}
