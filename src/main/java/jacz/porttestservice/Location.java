package jacz.porttestservice;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Alberto on 26/10/2015.
 */
public class Location extends HttpServlet {

    public static final class Result {

        private final String location;

        public Result(String location) {
            this.location = location;
        }

        public String getLocation() {
            return location;
        }
    }

    public static final String UNKNOWN_LOCATION = "UNKNOWN_LOCATION";

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        // todo assess location at init, with config in web.xml
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(new Result(UNKNOWN_LOCATION));
        response.getWriter().write(jsonResponse);
    }

}
