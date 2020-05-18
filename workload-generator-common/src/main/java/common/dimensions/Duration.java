package common.dimensions;

import java.util.concurrent.TimeUnit;

public class Duration {
  private final int duration;
  private final TimeUnit timeUnit;
  
  public Duration(final int duration, final TimeUnit timeUnit) {
    super();
    this.duration = duration;
    this.timeUnit = timeUnit;
  }

  public int getDuration() {
    return duration;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

}
