import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class LiftRideClient {
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final AtomicInteger successCount;
  private final AtomicInteger failureCount;
  private final CopyOnWriteArrayList<RequestRecord> latencyRecords;

  // Inner class for JSON serialization
  private static class LiftRideBody {
    public final int liftID;
    public final int time;

    public LiftRideBody(int liftID, int time) {
      this.liftID = liftID;
      this.time = time;
    }
  }

  // Inner class for latency tracking
  public static class RequestRecord {
    private final long startTime;
    private final String requestType;
    private final int latency;
    private final int responseCode;

    public RequestRecord(long startTime, String requestType,
        int latency, int responseCode) {
      this.startTime = startTime;
      this.requestType = requestType;
      this.latency = latency;
      this.responseCode = responseCode;
    }

    public long getStartTime() { return startTime; }
    public String getRequestType() { return requestType; }
    public int getLatency() { return latency; }
    public int getResponseCode() { return responseCode; }
  }

  public LiftRideClient() {
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(java.time.Duration.ofSeconds(10))
        .build();
    this.objectMapper = new ObjectMapper();
    this.successCount = new AtomicInteger(0);
    this.failureCount = new AtomicInteger(0);
    this.latencyRecords = new CopyOnWriteArrayList<>();
  }

  public void sendRequest(LiftRideEvent event, String serverBaseUrl) {
    int retryCount = 0;
    boolean success = false;
    long startTime = System.currentTimeMillis();
    int finalStatusCode = -1;

    while (retryCount < 5 && !success) {
      try {
        // Build URL
        String url = String.format("%s/skiers/resorts/%d/seasons/2025/days/1/skiers/%d",
            serverBaseUrl, event.getResortId(), event.getSkierId());

        // Create JSON body
        String jsonBody = objectMapper.writeValueAsString(
            new LiftRideBody(event.getLiftId(), event.getTime())
        );

        // Build request
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        // Send request and get response
        HttpResponse<String> response = httpClient.send(
            request, HttpResponse.BodyHandlers.ofString());

        long endTime = System.currentTimeMillis();
        finalStatusCode = response.statusCode();

        // Record latency
        latencyRecords.add(new RequestRecord(
            startTime,
            "POST",
            (int) (endTime - startTime),
            finalStatusCode
        ));

        // Handle response
        if (response.statusCode() == 201) {
          successCount.incrementAndGet();
          success = true;
        } else if (response.statusCode() >= 400 && response.statusCode() < 500) {
          // Client error - don't retry
          failureCount.incrementAndGet();
          break;
        } else {
          // Server error - retry
          retryCount++;
        }
      } catch (Exception e) {
        long endTime = System.currentTimeMillis();
        latencyRecords.add(new RequestRecord(
            startTime,
            "POST",
            (int) (endTime - startTime),
            -1  // Indicates network error
        ));
        retryCount++;
      }
    }

    if (!success && finalStatusCode >= 500) {
      failureCount.incrementAndGet();
    }
  }

  // Getters for metrics
  public int getSuccessCount() { return successCount.get(); }
  public int getFailureCount() { return failureCount.get(); }
  public CopyOnWriteArrayList<RequestRecord> getLatencyRecords() { return latencyRecords; }
}
