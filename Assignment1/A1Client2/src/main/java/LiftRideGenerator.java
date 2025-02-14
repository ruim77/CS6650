
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LiftRideGenerator {
  public static List<LiftRideEvent> generateAllEvents(int totalEvents) {
    List<LiftRideEvent> events = new ArrayList<>(totalEvents);
    Random rand = new Random();
    for (int i = 0; i < totalEvents; i++) {
      events.add(new LiftRideEvent(
          rand.nextInt(100_000) + 1,  // skierId: 1-100,000
          rand.nextInt(10) + 1,       // resortId: 1-10
          rand.nextInt(40) + 1,       // liftId: 1-40
          rand.nextInt(360) + 1       // time: 1-360
      ));
    }
    return events;
  }
}