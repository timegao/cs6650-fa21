package server;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import java.util.Map;

public class SQSClient {
    private static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID", AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY",
            AWS_SESSION_TOKEN = "AWS_SESSION_TOKEN";
    private static final Map<String, String> environments = new ProcessBuilder().environment();

    private static AmazonSQS instance;

    public static synchronized AmazonSQS getInstance() {
        if (instance == null) {
            instance = AmazonSQSClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicSessionCredentials(
                            environments.get(AWS_ACCESS_KEY_ID),
                            environments.get(AWS_SECRET_ACCESS_KEY),
                            environments.get(AWS_SESSION_TOKEN))))
                    .withRegion(Regions.US_EAST_1)
                    .build();
        }
        return instance;
    }
}
