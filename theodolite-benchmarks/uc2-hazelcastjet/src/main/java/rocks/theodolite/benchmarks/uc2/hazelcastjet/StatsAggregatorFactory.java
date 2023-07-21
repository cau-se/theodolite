package rocks.theodolite.benchmarks.uc2.hazelcastjet;

import com.google.common.math.Stats;
import com.google.common.math.StatsAccumulator;
import com.hazelcast.jet.aggregate.AggregateOperation;
import com.hazelcast.jet.aggregate.AggregateOperation1;
import java.util.Map.Entry;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;


/**
 * Factory for creating an aggregation operator for {@link Stats} objects.
 */
public final class StatsAggregatorFactory {

  private StatsAggregatorFactory() {}

  /**
   * Defines an AggregateOperation1 for Hazelcast Jet which is used in the Pipeline of the Hazelcast
   * Jet implementation of UC2.
   *
   * <p>
   * Takes a windowed and keyed {@code Entry<String,ActivePowerRecord>} elements and returns a
   * {@link Stats} object.
   * </p>
   *
   * @return An AggregateOperation used by Hazelcast Jet in a streaming stage which aggregates
   *         ActivePowerRecord Objects into Stats Objects.
   */
  public static AggregateOperation1<Entry<?, ActivePowerRecord>, StatsAccumulator, Stats> // NOCS
      create() {
    // Aggregate Operation to Create a Stats Object from Entry<?,ActivePowerRecord> items using
    // the StatsAccumulator.
    return AggregateOperation
        // Creates the accumulator
        .withCreate(new StatsAccumulatorSupplier())
        // Defines the accumulation
        .<Entry<?, ActivePowerRecord>>andAccumulate((accumulator, item) -> {
          accumulator.add(item.getValue().getValueInW());
        })
        // Defines the combination of spread out instances
        .andCombine((left, right) -> {
          final Stats rightStats = right.snapshot();
          left.addAll(rightStats);

        })
        // Finishes the aggregation
        .andExportFinish(StatsAccumulator::snapshot);
  }
}
