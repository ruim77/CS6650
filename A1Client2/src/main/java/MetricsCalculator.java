import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MetricsCalculator {
  public static void calculateAndPrint(List<LiftRideClient.RequestRecord> records, long wallTimeMillis) {
    if (records.isEmpty()) {
      System.out.println("No requests recorded - cannot calculate metrics");
      return;
    }

    // Extract and sort latencies
    List<Integer> sortedLatencies = records.stream()
        .map(LiftRideClient.RequestRecord::getLatency)
        .sorted()
        .collect(Collectors.toList());

    // Basic calculations
    int totalRequests = sortedLatencies.size();
    long totalLatency = sortedLatencies.stream().mapToLong(Integer::longValue).sum();

    // Percentile calculations
    double mean = (double) totalLatency / totalRequests;
    int min = sortedLatencies.get(0);
    int max = sortedLatencies.get(totalRequests - 1);
    double median = getPercentile(sortedLatencies, 50);
    double p99 = getPercentile(sortedLatencies, 99);
    double throughput = totalRequests / (wallTimeMillis / 1000.0);

    System.out.println("\n===== Performance Metrics =====");
    System.out.printf("Mean latency: %.2f ms%n", mean);
    System.out.printf("Median latency: %.2f ms%n", median);
    System.out.printf("p99 latency: %.2f ms%n", p99);
    System.out.printf("Min latency: %d ms%n", min);
    System.out.printf("Max latency: %d ms%n", max);
    System.out.printf("Throughput: %.2f requests/second%n", throughput);
  }

  private static double getPercentile(List<Integer> sortedLatencies, double percentile) {
    if (sortedLatencies.isEmpty()) return 0;

    double index = (percentile / 100.0) * sortedLatencies.size();
    int roundedIndex = (int) Math.ceil(index);

    // Handle edge case where index exceeds list size
    if (roundedIndex >= sortedLatencies.size()) {
      return sortedLatencies.get(sortedLatencies.size() - 1);
    }
    return sortedLatencies.get(roundedIndex - 1);
  }
}