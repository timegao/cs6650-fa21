package main.part2.task;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import lombok.RequiredArgsConstructor;
import main.part2.commandline.CommandLineInput;
import main.part2.model.Lift;
import main.part2.model.Request;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Each request creates numberRequest of post requests and random LiftRides
 */
@RequiredArgsConstructor
public final class PostRequestTask implements Runnable {
    private static final int RESORT_ID = 123, BAD_REQUEST = 400, OK_REQUEST = 200;
    private static final String SEASON_ID = "456", DAY_ID = "789";

    private final int numberLifts, numberRequests;
    private final String serverIPAddress;
    private final Lift lift;
    private final ConcurrentLinkedQueue<Request> successQueue, failureQueue;

    public PostRequestTask(CommandLineInput input, ConcurrentLinkedQueue<Request> successQueue,
                           ConcurrentLinkedQueue<Request> failureQueue, Lift lift, int numberRequests) {
        this.serverIPAddress = input.getServerIPAddress();
        this.numberLifts = input.getNumberLifts();
        this.lift = lift;
        this.numberRequests = numberRequests;
        this.successQueue = successQueue;
        this.failureQueue = failureQueue;
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
        long startTime = System.currentTimeMillis();
        int responseCode;
        try {
            apiInstance.writeNewLiftRide(liftRide, RESORT_ID, SEASON_ID, DAY_ID, skierID);
            responseCode = OK_REQUEST;
        } catch (ApiException e) {
            responseCode = BAD_REQUEST;
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        if (responseCode == OK_REQUEST) {
            successQueue.add(new Request(startTime, endTime, endTime - startTime, OK_REQUEST, "POST"));
        } else {
            failureQueue.add(new Request(startTime, endTime, endTime - startTime, BAD_REQUEST, "POST"));
        }
    }
}
