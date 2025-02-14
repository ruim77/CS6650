import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

public class CsvWriter {
  public static void writeLatencyRecords(
      List<LiftRideClient.RequestRecord> records,
      String filename
  ) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
      // Write CSV header
      writer.write("StartTime,RequestType,Latency,ResponseCode\n");

      // Write data rows
      for (LiftRideClient.RequestRecord record : records) {
        writer.write(String.format("%d,%s,%d,%d%n",
            record.getStartTime(),
            record.getRequestType(),
            record.getLatency(),
            record.getResponseCode()
        ));
      }

      System.out.println("Successfully wrote latency data to: " + filename);
    } catch (Exception e) {
      System.err.println("Failed to write CSV file: " + e.getMessage());
      e.printStackTrace();
    }
  }
}