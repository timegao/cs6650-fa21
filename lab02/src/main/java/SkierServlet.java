import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {
    int EMPTY_PARAMETER_INDEX = 0;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject jsonResponse = new JSONObject();
        String urlPath = request.getPathInfo();
        PrintWriter writer = response.getWriter();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            jsonResponse.put("message", "Missing GET parameters!");
            response.getWriter().write(jsonResponse.toString());
            return;
        }
        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!urlParts[EMPTY_PARAMETER_INDEX].equals("") || !isValidGetParameters(urlParts)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.put("message", "Invalid GET parameters: " + urlPath);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            // do any sophisticated processing with urlParts which contains all the url params
            // TODO: process url params in `urlParts`
            JSONObject resort1 = new JSONObject();
            resort1.put("seasonID", "2021");
            resort1.put("totalVert", 0);
            JSONArray array = new JSONArray();
            array.put(resort1);
            jsonResponse.put("resorts", array);
        }
        writer.write(jsonResponse.toString());
        writer.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject jsonResponse = new JSONObject();
        String urlPath = request.getPathInfo();
        PrintWriter writer = response.getWriter();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            jsonResponse.put("message", "Missing POST parameters!");
            writer.write(jsonResponse.toString());
            writer.close();
            return;
        }
        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!urlParts[EMPTY_PARAMETER_INDEX].equals("") || !isValidPostParameters(urlParts) || !isValidJsonBody(request)) {
//        if (!urlParts[EMPTY_PARAMETER_INDEX].equals("") || !isValidPostParameters(urlParts)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String requestString = request.getReader().lines().collect(Collectors.joining());
            jsonResponse.put("message", "Invalid POST parameters: " + requestString);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            // do any sophisticated processing with urlParts which contains all the url params
            // TODO: process url params in `urlParts`
        }
        writer.write(jsonResponse.toString());
        writer.close();
    }

    private boolean isValidGetParameters(@org.jetbrains.annotations.NotNull String[] urlPath) {
        final int VERTICAL_BY_DAY_PARAMETER_LENGTH = 8, VERTICAL_PARAMETER_LENGTH = 3;

        // TODO: validate the request url path according to the API spec
        if (urlPath.length == VERTICAL_BY_DAY_PARAMETER_LENGTH) {
            return isValidVerticalByDay(urlPath);
        } else if (urlPath.length == VERTICAL_PARAMETER_LENGTH) {
            return isValidVertical(urlPath);
        }
        return false;
//        return urlPath.length == VERTICAL_BY_DAY_PARAMETER_LENGTH || urlPath.length == VERTICAL_PARAMETER_LENGTH;
    }


    private boolean isValidPostParameters(@org.jetbrains.annotations.NotNull String[] urlParts) {
        // urlParts = "/{resortId}/seasons/{seasonId}/days/{dayId}/skiers/{skierID}"
        final int VERTICAL_BY_DAY_PARAMETER_LENGTH = 8;

        // TODO: validate the request url path according to the API spec
//        return urlParts.length == VERTICAL_BY_DAY_PARAMETER_LENGTH && isValidVerticalByDay(urlParts);
        return urlParts.length == VERTICAL_BY_DAY_PARAMETER_LENGTH;
    }

    /**
     * Example Value
     * {
     * "time": 217,
     * "liftID": 21
     * }
     */
    private boolean isValidJsonBody(@org.jetbrains.annotations.NotNull HttpServletRequest req) {
        final String TIME_PARAMETER = "time", LIFT_ID_PARAMETER = "liftID";
        try {
            String requestString = req.getReader().lines().collect(Collectors.joining());
            JSONObject jsonObject = new JSONObject(requestString);
            jsonObject.getInt(TIME_PARAMETER);
            jsonObject.getInt(LIFT_ID_PARAMETER);
            return true;
        } catch (IOException | JSONException e) {
            return false;
        }
    }

    /**
     * Example urlParts
     * <p>
     * "/{skierID}/vertical
     */
    private boolean isValidVertical(@org.jetbrains.annotations.NotNull String[] urlParts) {
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
    private boolean isValidVerticalByDay(@org.jetbrains.annotations.NotNull String[] urlParts) {
        final int RESORT_ID_INDEX = 1, SEASONS_INDEX = 2, SEASON_ID_INDEX = 3, DAYS_INDEX = 4,
                DAY_ID_INDEX = 5, SKIERS_INDEX = 6, SKIER_ID_INDEX = 7, MINIMUM_DAY_ID = 1, MAXIMUM_DAY_ID = 420;
        final String SKIERS_PARAMETER = "skiers", SEASONS_PARAMETER = "seasons", DAYS_PARAMETER = "days";
        if (urlParts[SEASONS_INDEX].equals(SEASONS_PARAMETER) && urlParts[DAYS_INDEX].equals(DAYS_PARAMETER)
                        && urlParts[SKIERS_INDEX].equals(SKIERS_PARAMETER))
        {
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
