package consumer.liftride;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import database.LiftRideDao;
import model.LiftRide;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class LiftRideListener implements MessageListener {

    private final LiftRideDao dao;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public LiftRideListener() {
        this.dao = new LiftRideDao();
    }

    @Override
    public void onMessage(Message message) {
        try {
            LiftRide liftRide = gson.fromJson(((TextMessage) message).getText(), LiftRide.class);
            dao.createLiftRide(liftRide);
            message.acknowledge();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
