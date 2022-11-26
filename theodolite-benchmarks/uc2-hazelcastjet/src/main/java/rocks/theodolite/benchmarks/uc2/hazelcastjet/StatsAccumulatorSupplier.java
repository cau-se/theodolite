package rocks.theodolite.benchmarks.uc2.hazelcastjet;

import com.google.common.math.StatsAccumulator;
import com.hazelcast.function.SupplierEx;

/**
 * Supplies a {@link StatsAccumulator}.
 */
public class StatsAccumulatorSupplier implements SupplierEx<StatsAccumulator> {

  private static final long serialVersionUID = -656395626316842910L; // NOPMD

  /**
   * Gets a {@link StatsAccumulator}.
   */
  @Override
  public StatsAccumulator getEx() throws Exception {
    return new StatsAccumulator();
  }

}
