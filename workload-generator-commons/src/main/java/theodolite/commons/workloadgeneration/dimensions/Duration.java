package theodolite.commons.workloadgeneration.dimensions;

import java.util.concurrent.TimeUnit;
import theodolite.commons.workloadgeneration.generators.AbstractWorkloadGenerator;

/**
 * Wrapper class for the definition of the duration for the {@link AbstractWorkloadGenerator}.
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
