package consumer;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import consumer.model.LiftRide;
import consumer.model.Skier;
import lombok.Data;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Data
public class RunnableChannelTask implements Runnable {
    private static final boolean DURABLE = true, EXCLUSIVE = false, AUTO_DELETE = false;
    private static final int MESSAGE_PER_RECEIVER = 1;

    private static final Logger logger = Logger.getLogger(RunnableChannelTask.class.getName());

    private final Connection connection;
    private final String queueName;
    private final Gson gson;
    private final ConcurrentHashMap<Integer, ConcurrentLinkedQueue<LiftRide>> hashMap;

    @Override
    public void run() {
        try {
            final Channel channel = connection.createChannel();
            channel.queueDeclare(queueName, DURABLE, EXCLUSIVE, AUTO_DELETE, null);
            channel.basicQos(MESSAGE_PER_RECEIVER);
            logger.info("[*] Thread waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                Skier skier = gson.fromJson(message, Skier.class);
                LiftRide liftRide = skier.getLiftRide();
                this.addToHashMap(skier.getId(), liftRide);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };
            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            logger.error("Failed to connect to queue or channel!");
            e.printStackTrace();
        }
    }

    private void addToHashMap(int id, LiftRide liftRide) {
        this.hashMap.computeIfPresent(id, (k, v) -> {
            v.add(liftRide);
            return v;
        });
    }
}
