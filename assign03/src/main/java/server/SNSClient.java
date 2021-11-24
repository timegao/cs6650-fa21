package server;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;

import java.util.Map;
import java.util.UUID;

public class SNSClient {
    private static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID", AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY",
            AWS_SESSION_TOKEN = "AWS_SESSION_TOKEN", TOPIC_ARN = "TOPIC_ARN", GROUP_ID = "GROUP_ID";
    private static final Map<String, String> environments = new ProcessBuilder().environment();

    private static AmazonSNS instance;

    public static synchronized AmazonSNS getInstance() {
        if (instance == null) {
            instance = AmazonSNSClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicSessionCredentials(
                            environments.get(AWS_ACCESS_KEY_ID),
                            environments.get(AWS_SECRET_ACCESS_KEY),
                            environments.get(AWS_SESSION_TOKEN))))
                    .withRegion(Regions.US_EAST_1)
                    .build();
        }
        return instance;
    }

    public static void publish(final String jsonResponse) {
        if (instance == null) {
            instance = getInstance();
        }

        instance.publish(
                new PublishRequest()
                        .withTopicArn(environments.get(TOPIC_ARN))
                        .withMessageGroupId(environments.get(GROUP_ID))
                        .withMessageDeduplicationId(UUID.randomUUID().toString())
                        .withMessage(jsonResponse));
    }
}
