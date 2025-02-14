//// LiftRideClient.java
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LiftRideClient {
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final AtomicInteger successCount;
  private final AtomicInteger failureCount;

  public LiftRideClient() {
    this.httpClient = HttpClient.newBuilder().build();
    this.objectMapper = new ObjectMapper();
    this.successCount = new AtomicInteger(0);
    this.failureCount = new AtomicInteger(0);
  }

  public void sendRequest(LiftRideEvent event, String serverUrl) {
    String url = String.format(
        "%s/skiers/resorts/%d/seasons/2025/days/1/skiers/%d",
        serverUrl, event.getResortId(), event.getSkierId()
    );

    try {
      String jsonBody = objectMapper.writeValueAsString(
          new LiftRideBody(event.getLiftId(), event.getTime())
      );

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
          .build();

      HttpResponse<String> response = httpClient.send(
          request, HttpResponse.BodyHandlers.ofString()
      );

      if (response.statusCode() == 201) {
        successCount.incrementAndGet();
      } else {
        failureCount.incrementAndGet();
      }
    } catch (Exception e) {
      failureCount.incrementAndGet();
    }
  }

  public int getSuccessCount() { return successCount.get(); }
  public int getFailureCount() { return failureCount.get(); }

  // Helper class for JSON serialization
  private static class LiftRideBody {
    public final int liftID;
    public final int time;
    public LiftRideBody(int liftID, int time) {
      this.liftID = liftID;
      this.time = time;
    }
  }
}


