package server;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.UUID;

public class SNSClient {
    private static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID", AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY",
            AWS_SESSION_TOKEN = "AWS_SESSION_TOKEN", TOPIC_ARN = "TOPIC_ARN";
    private static final Dotenv dotenv = Dotenv.configure().load();


    private static AmazonSNS instance;

    public static synchronized AmazonSNS getInstance() {
        if (instance == null) {
            instance = AmazonSNSClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicSessionCredentials(
                            dotenv.get(AWS_ACCESS_KEY_ID),
                            dotenv.get(AWS_SECRET_ACCESS_KEY),
                            dotenv.get(AWS_SESSION_TOKEN)
                    )))
                    .build();
        }
        return instance;
    }

    public static void publish(final String jsonResponse) {
        if (instance == null) {
            instance = getInstance();
        }

        String randomId = UUID.randomUUID().toString();

        instance.publish(
                new PublishRequest()
                        .withTopicArn(dotenv.get(TOPIC_ARN))
                        .withMessageGroupId(randomId)
                        .withMessageDeduplicationId(randomId)
                        .withMessage(jsonResponse));
    }
}
