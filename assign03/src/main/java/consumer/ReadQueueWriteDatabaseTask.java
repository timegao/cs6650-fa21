package consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import database.LiftRideDao;
import model.LiftRide;
import server.SQSClient;

import java.util.List;

public class ReadQueueWriteDatabaseTask implements Runnable {

    private final String queueName, queueUrl;
    private static final AmazonSQS sqs = SQSClient.getInstance();
    private final LiftRideDao dao = new LiftRideDao();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public ReadQueueWriteDatabaseTask(String queueName) {
        this.queueName = queueName;
        this.queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
    }

    @Override
    public void run() {
        while (true) {
            List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
            for (Message message : messages) {
                LiftRide liftRide = gson.fromJson(message.getBody(), LiftRide.class);
                dao.createLiftRide(liftRide);
                sqs.deleteMessage(queueUrl, message.getReceiptHandle());
            }
        }
    }
}
