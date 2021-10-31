package consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import consumer.commandline.ConsumerCommandLineInput;
import consumer.commandline.ConsumerCommandLineOptions;
import consumer.model.LiftRide;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class SkierConsumer {

    public static void main(String[] args) throws Exception {

        Options instance = ConsumerCommandLineOptions.getInstance();
        ConsumerCommandLineInput input = null;

        try {
            input = new ConsumerCommandLineInput(new DefaultParser().parse(instance, args));
        } catch (ParseException | IllegalArgumentException e) {
            e.printStackTrace();
            new HelpFormatter().printHelp("options", instance);
            System.exit(1);
        }

        ConcurrentHashMap<Integer, ConcurrentLinkedQueue<LiftRide>> hashMap = new ConcurrentHashMap<>();
        populateHashMap(hashMap, input);
        BasicConfigurator.configure();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(input.getHostName());
        factory.setUsername(input.getUsername());
        factory.setPassword(input.getPassword());
        final Connection connection = factory.newConnection();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        for (int i = 0; i < input.getNumberThreads(); i++) {
            new Thread(new RunnableChannelTask(connection, input.getQueueName(), gson, hashMap)).start();
        }
    }

    private static void populateHashMap(ConcurrentHashMap<Integer, ConcurrentLinkedQueue<LiftRide>> hashMap,
                                        ConsumerCommandLineInput input) {
        for (int i = 0; i < input.getNumberSkiers(); i++) {
            hashMap.put(i, new ConcurrentLinkedQueue<>());
        }
    }
}
