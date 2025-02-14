  // ClientMain.java
  import java.util.List;
  import java.util.concurrent.ExecutorService;
  import java.util.concurrent.Executors;
  import java.util.concurrent.TimeUnit;
  import java.util.concurrent.atomic.AtomicInteger;

  public class ClientMain {
    private static final int TOTAL_EVENTS = 200_000;
    private static final int INITIAL_THREADS = 32;
    private static final int REQUESTS_PER_INITIAL_THREAD = 1000;

    public static void main(String[] args) throws InterruptedException {
      String serverUrl = "http://52.41.138.156:8080/Assignment1Server_war";
      List<LiftRideEvent> allEvents = LiftRideGenerator.generateAllEvents(TOTAL_EVENTS);
      LiftRideClient client = new LiftRideClient();

      long startTime = System.currentTimeMillis();

      // Phase 1: Initial 32 threads (1,000 requests each)
      ExecutorService initialPool = Executors.newFixedThreadPool(INITIAL_THREADS);
      for (int i = 0; i < INITIAL_THREADS; i++) {
        int startIdx = i * REQUESTS_PER_INITIAL_THREAD;
        int endIdx = startIdx + REQUESTS_PER_INITIAL_THREAD;
        initialPool.submit(() -> {
          for (int j = startIdx; j < endIdx; j++) {
            client.sendRequest(allEvents.get(j), serverUrl);
          }
        });
      }
      initialPool.shutdown();
      initialPool.awaitTermination(1, TimeUnit.HOURS);

      // Phase 2: Dynamic threads for remaining events
      // Create threads proportional to CPU cores
      int availableCores = Runtime.getRuntime().availableProcessors();
      int dynamicThreads = availableCores * 12;
      ExecutorService dynamicPool = Executors.newFixedThreadPool(dynamicThreads);
      AtomicInteger currentIndex = new AtomicInteger(INITIAL_THREADS * REQUESTS_PER_INITIAL_THREAD);

      System.out.println("\n===== Phase 2 Starting =====");
      System.out.println("Available CPU cores: " + availableCores);
      System.out.println("Dynamic threads: " + dynamicThreads);
      for (int i = 0; i < dynamicThreads; i++) {
        dynamicPool.submit(() -> {
          while (currentIndex.get() < TOTAL_EVENTS) {
            int idx = currentIndex.getAndIncrement();
            if (idx < TOTAL_EVENTS) {
              client.sendRequest(allEvents.get(idx), serverUrl);
            }
          }
        });
      }

      dynamicPool.shutdown();
      dynamicPool.awaitTermination(1, TimeUnit.HOURS);



      // Calculate metrics
      long endTime = System.currentTimeMillis();
      long totalTime = endTime - startTime;
      double throughput = (double) TOTAL_EVENTS / (totalTime / 1000.0);

      System.out.println("===== Results =====");
      System.out.println("Successful requests: " + client.getSuccessCount());
      System.out.println("Failed requests: " + client.getFailureCount());
      System.out.printf("Total time: %d ms%n", totalTime);
      System.out.printf("Throughput: %.2f requests/second%n", throughput);
    }
  }


