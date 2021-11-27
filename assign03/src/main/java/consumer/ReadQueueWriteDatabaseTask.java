package consumer;

import com.amazonaws.internal.SdkInternalList;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import database.TableDao;
import model.LiftRide;
import org.apache.log4j.Logger;
import server.SQSClient;

import java.util.List;

public class ReadQueueWriteDatabaseTask implements Runnable {

    private final String queueUrl;
    private static final AmazonSQS sqs = SQSClient.getInstance();
    private final TableDao dao;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = Logger.getLogger(ReadQueueWriteDatabaseTask.class.getName());


    public ReadQueueWriteDatabaseTask(String queueName) {
        this.queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
        this.dao = new TableDao(queueName);
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        logger.info("[*] Thread waiting for messages. To exit press CTRL+C");
        while (true) {
            List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
            SdkInternalList<DeleteMessageBatchRequestEntry> entries = new SdkInternalList<>();
            for (Message message : messages) {
                LiftRide liftRide = gson.fromJson(message.getBody(), LiftRide.class);
                dao.createLiftRide(liftRide);
                entries.add(new DeleteMessageBatchRequestEntry(message.getMessageId(), message.getReceiptHandle()));
            }
            if (entries.size() > 0) {
                sqs.deleteMessageBatch(new DeleteMessageBatchRequest(queueUrl, entries));
            }
        }
    }
}
