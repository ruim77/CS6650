import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SkiersServlet extends HttpServlet {
  private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

      response.setContentType("application/json");

      try {
        // Validate Content-Type
        if (!"application/json".equalsIgnoreCase(request.getContentType())) {
          sendError(response, 415, "Content-Type must be application/json");
          return;
        }

        // Validate URL structure
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
          sendError(response, 400, "Invalid URL format");
          return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length != 9
            || !pathParts[1].equals("resorts")
            || !pathParts[3].equals("seasons")
            || !pathParts[5].equals("days")
            || !pathParts[7].equals("skiers")) {
          sendError(response, 400, "Invalid URL structure");
          return;
        }

        // Extract and validate parameters
        int resortId = Integer.parseInt(pathParts[2]);
        String seasonId = pathParts[4];
        int dayId = Integer.parseInt(pathParts[6]);
        int skierId = Integer.parseInt(pathParts[8]);

        // Parameter validation
        if (resortId < 1 || resortId > 10) {
          sendError(response, 400, "resortID must be between 1-10");
          return;
        }
        if (!seasonId.equals("2025")) {
          sendError(response, 400, "seasonID must be 2025");
          return;
        }
        if (dayId != 1) {
          sendError(response, 400, "dayID must be 1");
          return;
        }
        if (skierId < 1 || skierId > 100000) {
          sendError(response, 400, "skierID must be between 1-100000");
          return;
        }

        // Parse and validate JSON body
        LiftRide liftRide = objectMapper.readValue(request.getInputStream(), LiftRide.class);
        if (liftRide.liftID == null || liftRide.liftID < 1 || liftRide.liftID > 40) {
          sendError(response, 400, "liftID must be between 1-40");
          return;
        }
        if (liftRide.time == null || liftRide.time < 1 || liftRide.time > 360) {
          sendError(response, 400, "time must be between 1-360");
          return;
        }

        // Success response
        response.setStatus(201);
        objectMapper.writeValue(response.getWriter(), new Response("Lift ride recorded"));

      } catch (NumberFormatException e) {
        sendError(response, 400, "Invalid numeric parameter format");
      } catch (IOException e) {
        sendError(response, 400, "Invalid JSON format");
      }
    }

    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
      response.setStatus(code);
      objectMapper.writeValue(response.getWriter(), new Error(message));
    }

    // DTO classes
    private static class LiftRide {
      public Integer liftID;
      public Integer time;
    }

    private static class Response {
      public final String message;
      public Response(String message) { this.message = message; }
    }

    private static class Error {
      public final String error;
      public Error(String error) { this.error = error; }
    }
}





