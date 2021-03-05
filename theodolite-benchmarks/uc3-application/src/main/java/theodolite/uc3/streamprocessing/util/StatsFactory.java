package theodolite.uc3.streamprocessing.util;

import com.google.common.math.Stats;
import com.google.common.math.StatsAccumulator;

/**
 * Factory methods for working with {@link Stats}.
 */
public final class StatsFactory {

  private StatsFactory() {}

  /**
   * Add a value to a {@link Stats} object.
   */
  public static Stats accumulate(final Stats stats, final double value) {
    final StatsAccumulator statsAccumulator = new StatsAccumulator();
    statsAccumulator.addAll(stats);
    statsAccumulator.add(value);
    return statsAccumulator.snapshot();
  }

}
