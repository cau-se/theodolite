package rocks.theodolite.benchmarks.uc2.hazelcastjet;

import com.google.common.math.StatsAccumulator;
import com.hazelcast.function.SupplierEx;

/**
 * Supplies a StatsAccumulator. Is used in the aggregation operation of the Hazelcast Jet
 * implementation for UC2.
 */
public class StatsAccumulatorSupplier implements SupplierEx<StatsAccumulator> {

  private static final long serialVersionUID = -656395626316842910L; // NOPMD

  /**
   * Gets a StatsAccumulator.
   */
  @Override
  public StatsAccumulator getEx() throws Exception {
    return new StatsAccumulator();
  }

}
