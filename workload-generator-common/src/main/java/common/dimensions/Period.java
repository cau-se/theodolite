package common.dimensions;

import java.util.concurrent.TimeUnit;

public class Period {
  
  private final int period;
  private final TimeUnit timeUnit;
  
  public Period(final int period, final TimeUnit timeUnit) {
    super();
    this.period = period;
    this.timeUnit = timeUnit;
  }

  public int getDuration() {
    return period;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

}
