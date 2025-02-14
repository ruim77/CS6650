//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//public class ClientMain {
//  private static final int TOTAL_REQUESTS = 200_000;
//  private static final int INITIAL_THREADS = 32;
//  private static final int REQUESTS_PER_INITIAL_THREAD = 1000;
//
//  public static void main(String[] args) {
//
//    String serverBaseUrl = "http://35.91.164.84:8080/Assignment1Server_war";
//    System.out.println("Starting client with server: " + serverBaseUrl);
//
//    // Generate all events upfront in single thread
//    List<LiftRideEvent> events = LiftRideGenerator.generateAllEvents(TOTAL_REQUESTS);
//    LiftRideClient client = new LiftRideClient();
//
//    long startTime = System.currentTimeMillis();
//
//    try {
//      // Phase 1: Initial 32 threads x 1000 requests each
//      ExecutorService initialExecutor = Executors.newFixedThreadPool(INITIAL_THREADS);
//      for (int i = 0; i < INITIAL_THREADS; i++) {
//        final int threadIndex = i;
//        initialExecutor.submit(() -> {
//          int start = threadIndex * REQUESTS_PER_INITIAL_THREAD;
//          int end = start + REQUESTS_PER_INITIAL_THREAD;
//          for (int j = start; j < end; j++) {
//            client.sendRequest(events.get(j), serverBaseUrl);
//          }
//        });
//      }
//      initialExecutor.shutdown();
//      initialExecutor.awaitTermination(1, TimeUnit.HOURS);
//
//      // Phase 2: Dynamic threads for remaining requests
//      AtomicInteger currentIndex = new AtomicInteger(INITIAL_THREADS * REQUESTS_PER_INITIAL_THREAD);
//      int remainingRequests = TOTAL_REQUESTS - (INITIAL_THREADS * REQUESTS_PER_INITIAL_THREAD);
//
//      System.out.printf("\nStarting dynamic phase with %d remaining requests\n", remainingRequests);
//
//      ExecutorService dynamicExecutor = Executors.newCachedThreadPool();
//      int availableProcessors = Runtime.getRuntime().availableProcessors();
//      for (int i = 0; i < availableProcessors * 2; i++) {
//        dynamicExecutor.submit(() -> {
//          while (currentIndex.get() < TOTAL_REQUESTS) {
//            int idx = currentIndex.getAndIncrement();
//            if (idx < TOTAL_REQUESTS) {
//              client.sendRequest(events.get(idx), serverBaseUrl);
//            }
//          }
//        });
//      }
//      dynamicExecutor.shutdown();
//      dynamicExecutor.awaitTermination(1, TimeUnit.HOURS);
//
//    } catch (InterruptedException e) {
//      System.err.println("Thread execution interrupted: " + e.getMessage());
//      Thread.currentThread().interrupt();
//    }
//
//    long endTime = System.currentTimeMillis();
//    long totalTime = endTime - startTime;
//
//    // Write results to CSV
//    CsvWriter.writeLatencyRecords(client.getLatencyRecords(), "latency_results.csv");
//
//    // Calculate and print metrics
//    MetricsCalculator.calculateAndPrint(client.getLatencyRecords(), totalTime);
//
//    // Print summary
//    System.out.println("\n===== Request Summary =====");
//    System.out.println("Successful requests: " + client.getSuccessCount());
//    System.out.println("Failed requests: " + client.getFailureCount());
//    System.out.printf("Total wall time: %.2f seconds%n", totalTime / 1000.0);
//    System.out.printf("Throughput: %.2f requests/second%n",
//        TOTAL_REQUESTS / (totalTime / 1000.0));
//  }
//}

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientMain {
  private static final int TOTAL_EVENTS = 200_000;
  private static final int INITIAL_THREADS = 32;
  private static final int REQUESTS_PER_INITIAL_THREAD = 1000;

  public static void main(String[] args) {


    String serverUrl = "http://52.41.138.156:8080/Assignment1Server_war";
    List<LiftRideEvent> allEvents = LiftRideGenerator.generateAllEvents(TOTAL_EVENTS);
    LiftRideClient client = new LiftRideClient();

    long startTime = System.currentTimeMillis();

    try {
      // Phase 1: Initial 32 threads (1,000 requests each)
      ExecutorService initialPool = Executors.newFixedThreadPool(INITIAL_THREADS);
      System.out.printf("Starting Phase 1: %d threads, %d requests each\n",
          INITIAL_THREADS, REQUESTS_PER_INITIAL_THREAD);

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
      int remainingRequests = TOTAL_EVENTS - (INITIAL_THREADS * REQUESTS_PER_INITIAL_THREAD);
      int dynamicThreads = Runtime.getRuntime().availableProcessors() * 16;
      System.out.printf("\nStarting Phase 2: %d dynamic threads for %d requests\n",
          dynamicThreads, remainingRequests);

      AtomicInteger currentIndex = new AtomicInteger(INITIAL_THREADS * REQUESTS_PER_INITIAL_THREAD);
      ExecutorService dynamicPool = Executors.newFixedThreadPool(dynamicThreads);

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

    } catch (InterruptedException e) {
      System.err.println("Thread execution interrupted: " + e.getMessage());
      Thread.currentThread().interrupt();
    }

    // Calculate metrics
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;

    // Part 2: Write CSV and calculate advanced metrics
    CsvWriter.writeLatencyRecords(client.getLatencyRecords(), "latency_results.csv");
    MetricsCalculator.calculateAndPrint(client.getLatencyRecords(), totalTime);

    // Part 1: Summary output
    System.out.println("\n===== Request Summary =====");
    System.out.println("Successful requests: " + client.getSuccessCount());
    System.out.println("Failed requests: " + client.getFailureCount());
    System.out.printf("Total wall time: %.2f seconds\n", totalTime / 1000.0);
    System.out.printf("Throughput: %.2f requests/second\n",
        TOTAL_EVENTS / (totalTime / 1000.0));
  }
}