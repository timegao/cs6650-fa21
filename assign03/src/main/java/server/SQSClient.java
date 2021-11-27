package server;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import io.github.cdimascio.dotenv.Dotenv;

public class SQSClient {
    private static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID", AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY",
            AWS_SESSION_TOKEN = "AWS_SESSION_TOKEN";
    private static final Dotenv dotenv = Dotenv.configure().load();

    private static AmazonSQS instance;

    public static synchronized AmazonSQS getInstance() {
        if (instance == null) {
            instance = AmazonSQSClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicSessionCredentials(
                            dotenv.get(AWS_ACCESS_KEY_ID),
                            dotenv.get(AWS_SECRET_ACCESS_KEY),
                            dotenv.get(AWS_SESSION_TOKEN))))
                    .build();
        }
        return instance;
    }
}
