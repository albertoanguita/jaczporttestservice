package jacz.porttestservice;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Service for monitoring server status and load
 */
public class ServerStatus extends HttpServlet {

    public static final class Result {

        private final String status;

        /**
         * A value between 0 and 1 that indicates the current server load
         */
        private final float serverLoad;

        public Result(float serverLoad) {
            this.status = "OK";
            this.serverLoad = serverLoad;
        }

        public String getStatus() {
            return status;
        }

        public float getServerLoad() {
            return serverLoad;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        // todo calculate server load
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(new Result(0f));
        response.getWriter().write(jsonResponse);
    }

}
