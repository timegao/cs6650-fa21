package consumer;

import com.amazon.sqs.javamessaging.*;
import consumer.commandline.ConsumerCommandLineInput;
import consumer.commandline.ConsumerCommandLineOptions;
import consumer.liftride.LiftRideListener;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import server.SQSClient;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

public class QueueConsumer {
    public static void main(String[] args) throws JMSException {
        Options instance = ConsumerCommandLineOptions.getInstance();
        ConsumerCommandLineInput input = null;

        try {
            input = new ConsumerCommandLineInput(new DefaultParser().parse(instance, args));
        } catch (ParseException | IllegalArgumentException e) {
            e.printStackTrace();
            new HelpFormatter().printHelp("options", instance);
            System.exit(1);
        }

        BasicConfigurator.configure();

        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                SQSClient.getInstance()
        );

        String queueName = input.getQueueName();

        SQSConnection connection = connectionFactory.createConnection();
        AmazonSQSMessagingClientWrapper client = connection.getWrappedAmazonSQSClient();
        if (!client.queueExists(queueName)) {
            client.createQueue(queueName);
        }

        for (int i = 0; i < input.getNumberThreads(); i++) {
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            Queue queue = session.createQueue(queueName);
            MessageConsumer consumer = session.createConsumer(queue);
            consumer.setMessageListener(new LiftRideListener());
        }
        connection.start();
    }
}
