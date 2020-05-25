package common.dimensions;

import java.util.concurrent.TimeUnit;
import common.generators.WorkloadGenerator;

/**
 * Wrapper class for the definition of the duration for the {@link WorkloadGenerator}.
 */
public class Duration {

  private final int duration;
  private final TimeUnit timeUnit;

  /**
   * Define a new duration.
   *
   * @param duration the duration
   * @param timeUnit the time unit that applies to the specified {@code duration}
   */
  public Duration(final int duration, final TimeUnit timeUnit) {
    super();
    this.duration = duration;
    this.timeUnit = timeUnit;
  }

  public int getDuration() {
    return this.duration;
  }

  public TimeUnit getTimeUnit() {
    return this.timeUnit;
  }

}
