package theodolite.commons.workloadgeneration.dimensions;

import java.util.concurrent.TimeUnit;
import theodolite.commons.workloadgeneration.generators.AbstractWorkloadGenerator;

/**
 * Wrapper class for the definition of period to use for the {@link AbstractWorkloadGenerator}.
 */
public class Period {

  private final int period;
  private final TimeUnit timeUnit;

  /**
   * Define a new period.
   *
   * @param period the period
   * @param timeUnit the time unit that applies to the specified {@code period}
   */
  public Period(final int period, final TimeUnit timeUnit) {
    super();
    this.period = period;
    this.timeUnit = timeUnit;
  }

  public int getPeriod() {
    return this.period;
  }

  public TimeUnit getTimeUnit() {
    return this.timeUnit;
  }

}
