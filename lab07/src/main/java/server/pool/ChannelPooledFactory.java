package server.pool;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Getter;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Getter
public class ChannelPooledFactory extends BasePooledObjectFactory<Channel> {

    private static final String QUEUE_NAME = "test";

    private final ConnectionFactory factory;
    private final Connection connection;
    private final String queueName;

    public ChannelPooledFactory() throws IOException, TimeoutException {
        this.factory = new ConnectionFactory();
        this.queueName = QUEUE_NAME;
        this.connection = factory.newConnection();
    }

    @Override
    public Channel create() throws Exception {
        final boolean DURABLE = true, EXCLUSIVE = false, AUTO_DELETE = false;
        Channel channel = this.connection.createChannel();
        channel.queueDeclare(this.queueName, DURABLE, EXCLUSIVE, AUTO_DELETE, null);
        return channel;
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<>(channel);
    }
}
