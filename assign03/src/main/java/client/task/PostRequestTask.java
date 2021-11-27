package client.task;

import client.commandline.ClientCommandLineInput;
import client.model.Lift;
import client.model.Request;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import lombok.RequiredArgsConstructor;
import org.apache.log4j.BasicConfigurator;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Each request creates numberRequest of post requests and random LiftRides
 */
@RequiredArgsConstructor
public final class PostRequestTask implements Runnable {
    private static final int RESORT_ID = 123, MAXIMUM_RETRIES = 3, INITIAL_SLEEP_TIMER = 1, SLEEP_TIMER_MULTIPLIER = 10;
    private static final String SEASON_ID = "456", DAY_ID = "123";

    private final int numberLifts, numberRequests;
    private final String serverIPAddress;
    private final Lift lift;
    private final ConcurrentLinkedQueue<Request> successQueue, failureQueue;

    public PostRequestTask(ClientCommandLineInput input, ConcurrentLinkedQueue<Request> successQueue,
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
        BasicConfigurator.configure();

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
            ApiResponse<Void> response = apiInstance.writeNewLiftRideWithHttpInfo(liftRide, RESORT_ID, SEASON_ID, DAY_ID, skierID);
            responseCode = response.getStatusCode();
        } catch (ApiException e) {
            responseCode = exponentialBackoff(apiInstance, liftRide, skierID, e.getCode());
        }
        long endTime = System.currentTimeMillis();
        if (responseCode == HttpServletResponse.SC_OK) {
            successQueue.add(new Request(startTime, endTime, endTime - startTime, responseCode, "POST"));
        } else {
            failureQueue.add(new Request(startTime, endTime, endTime - startTime, responseCode, "POST"));
        }
    }

    private int exponentialBackoff(SkiersApi apiInstance, LiftRide liftRide, int skierID, int apiExceptionCode) {
        int retries = 0;
        int sleepTimer = INITIAL_SLEEP_TIMER;
        int responseCode = apiExceptionCode;
        while (retries < MAXIMUM_RETRIES) {
            try {
                Thread.sleep(sleepTimer *= SLEEP_TIMER_MULTIPLIER);
                ApiResponse<Void> response = apiInstance.writeNewLiftRideWithHttpInfo(liftRide, RESORT_ID, SEASON_ID, DAY_ID, skierID);
                responseCode = response.getStatusCode();
                break;
            } catch (ApiException | InterruptedException e) {
                retries++;
                if (retries == MAXIMUM_RETRIES) {
                    e.printStackTrace();
                }
            }
        }
        return responseCode;
    }
}
