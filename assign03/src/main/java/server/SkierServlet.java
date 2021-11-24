package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.LiftInfo;
import model.LiftRide;
import org.apache.log4j.BasicConfigurator;
import server.message.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

@WebServlet(name = "server.SkierServlet", value = "/server.SkierServlet")
public class SkierServlet extends HttpServlet {

    private static final int EMPTY_PARAMETER_INDEX = 0, VERTICAL_BY_DAY_PARAMETER_LENGTH = 8, VERTICAL_PARAMETER_LENGTH = 3;

    private Gson gson;

    public void init() {
        try {
            this.gson = new GsonBuilder().setPrettyPrinting().create();
            BasicConfigurator.configure();
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

        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            jsonResponse = this.gson.toJson(new MissingGetMessage());
            writer.write(jsonResponse);
            writer.close();
            return;
        }
        String[] urlParts = urlPath.split("/");

        if (!urlParts[EMPTY_PARAMETER_INDEX].equals("") || !isValidGetParameters(urlParts)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse = this.gson.toJson(new InvalidGetMessage());
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            jsonResponse = this.gson.toJson(new SuccessGetMessage());
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

        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writer.write(this.gson.toJson(new MissingPostMessage()));
            writer.close();
            return;
        }
        String[] urlParts = urlPath.split("/");
        String requestString = request.getReader().lines().collect(Collectors.joining());

        if (!urlParts[EMPTY_PARAMETER_INDEX].equals("") || !isValidPostParameters(urlParts) || !isValidJsonBody(requestString)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse = this.gson.toJson(new InvalidPostMessage());
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            final int RESORT_ID_INDEX = 1, SEASON_ID_INDEX = 3, DAY_ID_INDEX = 5,
                    SKIER_ID_INDEX = 7;
            LiftInfo liftInfo = this.gson.fromJson(requestString, LiftInfo.class);
            LiftRide liftRide = new LiftRide(
                    Integer.parseInt(urlParts[SKIER_ID_INDEX]),
                    Integer.parseInt(urlParts[RESORT_ID_INDEX]),
                    Integer.parseInt(urlParts[SEASON_ID_INDEX]),
                    Integer.parseInt(urlParts[DAY_ID_INDEX]),
                    liftInfo.getTime(),
                    liftInfo.getLiftRideId());
            jsonResponse = this.gson.toJson(liftRide, LiftRide.class);
            SNSClient.publish(jsonResponse);
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
            this.gson.fromJson(requestString, LiftRide.class);
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
}
