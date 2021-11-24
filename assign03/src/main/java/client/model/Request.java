package client.model;

import lombok.Data;

/**
 * Stores information for each request
 */
@Data
public class Request {
    private final long startTime, endTime, responseTime;
    private final int responseCode;
    private final String requestType;

    @Override
    public String toString() {
        return startTime + "," + endTime + "," + + responseTime + "," + responseCode + "," + requestType + System.lineSeparator();
    }
}
