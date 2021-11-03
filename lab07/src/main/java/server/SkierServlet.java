package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import db.SkierDao;
import db.model.LiftRide;
import db.model.Skier;
import org.apache.commons.pool2.impl.GenericObjectPool;
import server.message.*;
import server.pool.ChannelPooledFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

@WebServlet(name = "server.SkierServlet", value = "/server.SkierServlet")
public class SkierServlet extends HttpServlet {

    private static final int EMPTY_PARAMETER_INDEX = 0;
    private static final int VERTICAL_BY_DAY_PARAMETER_LENGTH = 8, VERTICAL_PARAMETER_LENGTH = 3;
//    private static final String EXCHANGE_NAME = "";

    private ChannelPooledFactory factory;
    private GenericObjectPool<Channel> pool;
    private Gson gson;
    private SkierDao skierDao;

    public void init() {
        try {
            this.factory = new ChannelPooledFactory();
            this.pool = new GenericObjectPool<>(factory);
            this.gson = new GsonBuilder().setPrettyPrinting().create();
            this.skierDao = new SkierDao();

//            this.pool.setMaxTotal(-1);  // set pool size to infinite
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String urlPath = request.getPathInfo();
        PrintWriter writer = response.getWriter();
        String jsonResponse;

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            jsonResponse = gson.toJson(new MissingGetMessage());
            writer.write(jsonResponse);
            writer.close();
            return;
        }
        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!urlParts[EMPTY_PARAMETER_INDEX].equals("") || !isValidGetParameters(urlParts)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse = gson.toJson(new InvalidGetMessage());
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            // do any sophisticated processing with urlParts which contains all the url params
            jsonResponse = gson.toJson(new SuccessGetMessage());
        }
        writer.write(jsonResponse);
        writer.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String urlPath = request.getPathInfo();
        PrintWriter writer = response.getWriter();
        String jsonResponse;

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writer.write(gson.toJson(new MissingPostMessage()));
            writer.close();
            return;
        }
        String[] urlParts = urlPath.split("/");
        String requestString = request.getReader().lines().collect(Collectors.joining());
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!urlParts[EMPTY_PARAMETER_INDEX].equals("") || !isValidPostParameters(urlParts) || !isValidJsonBody(requestString)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse = gson.toJson(new InvalidPostMessage());
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            // do any sophisticated processing with urlParts which contains all the url params
            final int RESORT_ID_INDEX = 1, SEASON_ID_INDEX = 3, DAY_ID_INDEX = 5,
                    SKIER_ID_INDEX = 7;
            LiftRide liftRide = gson.fromJson(requestString, LiftRide.class);
            Skier skier =  new Skier(
                            Integer.parseInt(urlParts[SKIER_ID_INDEX]),
                            Integer.parseInt(urlParts[RESORT_ID_INDEX]),
                            Integer.parseInt(urlParts[SEASON_ID_INDEX]),
                            Integer.parseInt(urlParts[DAY_ID_INDEX]),
                            liftRide.getTime(),
                            liftRide.getLiftRideId());
            jsonResponse = gson.toJson(skier, Skier.class);
            skierDao.createLiftRide(skier);
//            sendMessageToQueue(jsonResponse);
        }
        writer.write(jsonResponse);
        writer.close();
    }

    private boolean isValidGetParameters(String[] urlPath) {
        if (urlPath.length == VERTICAL_BY_DAY_PARAMETER_LENGTH) {
            return isValidVerticalByDay(urlPath);
        } else if (urlPath.length == VERTICAL_PARAMETER_LENGTH) {
            return isValidVertical(urlPath);
        }
        return false;
    }


    private boolean isValidPostParameters(String[] urlParts) {
        return urlParts.length == VERTICAL_BY_DAY_PARAMETER_LENGTH && isValidVerticalByDay(urlParts);
    }

    /**
     * Example Value
     * {
     * "time": 217,
     * "liftID": 21
     * }
     */
    private boolean isValidJsonBody(String requestString) {
        try {
            gson.fromJson(requestString, Skier.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Example urlParts
     * <p>
     * "/{skierID}/vertical
     */
    private boolean isValidVertical(String[] urlParts) {
        final int SKIER_ID_INDEX = 1, VERTICAL_INDEX = 2;
        final String VERTICAL_PARAMETER = "vertical";
        if (urlParts[VERTICAL_INDEX].equals(VERTICAL_PARAMETER)) {
            try {
                Integer.parseInt(urlParts[SKIER_ID_INDEX]);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Example urlParts:
     * <p>
     * "/{resortId}/seasons/{seasonId}/days/{dayId}/skiers/{skierID}"
     */
    private boolean isValidVerticalByDay(String[] urlParts) {
        final int RESORT_ID_INDEX = 1, SEASONS_INDEX = 2, SEASON_ID_INDEX = 3, DAYS_INDEX = 4,
                DAY_ID_INDEX = 5, SKIERS_INDEX = 6, SKIER_ID_INDEX = 7, MINIMUM_DAY_ID = 1, MAXIMUM_DAY_ID = 420;
        final String SKIERS_PARAMETER = "skiers", SEASONS_PARAMETER = "seasons", DAYS_PARAMETER = "days";
        if (urlParts[SEASONS_INDEX].equals(SEASONS_PARAMETER) && urlParts[DAYS_INDEX].equals(DAYS_PARAMETER)
                && urlParts[SKIERS_INDEX].equals(SKIERS_PARAMETER)) {
            try {
                Integer.parseInt(urlParts[RESORT_ID_INDEX]);
                Integer.parseInt(urlParts[SEASON_ID_INDEX]);
                Integer.parseInt(urlParts[SKIER_ID_INDEX]);
                int dayId = Integer.parseInt(urlParts[DAY_ID_INDEX]);
                return dayId >= MINIMUM_DAY_ID && dayId <= MAXIMUM_DAY_ID;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

//    private void sendMessageToQueue(String message) {
//        try {
//            Channel channel = pool.borrowObject();
//            channel.basicPublish(EXCHANGE_NAME, this.factory.getQueueName(),
//                    MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes(StandardCharsets.UTF_8));
//            pool.returnObject(channel);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
