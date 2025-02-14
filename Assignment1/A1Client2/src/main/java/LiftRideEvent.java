public class LiftRideEvent {
  private final int skierId;
  private final int resortId;
  private final int liftId;
  private final int time;

  public LiftRideEvent(int skierId, int resortId, int liftId, int time) {
    this.skierId = skierId;
    this.resortId = resortId;
    this.liftId = liftId;
    this.time = time;
  }

  // Getters
  public int getSkierId() { return skierId; }
  public int getResortId() { return resortId; }
  public int getLiftId() { return liftId; }
  public int getTime() { return time; }
}