package rocks.theodolite.benchmarks.uc3.beam;

import com.google.common.math.Stats;
import com.google.common.math.StatsAccumulator;
import java.io.Serializable;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.transforms.Combine.CombineFn;
import rocks.theodolite.commons.model.records.ActivePowerRecord;


/**
 * Aggregation Class for ActivePowerRecords. Creates a StatsAccumulator based on the ValueInW.
 */

@DefaultCoder(AvroCoder.class)
public class StatsAggregation extends CombineFn<ActivePowerRecord, StatsAccumulator, Stats>
    implements Serializable {
  private static final long serialVersionUID = 1L;

  @Override
  public StatsAccumulator createAccumulator() {
    return new StatsAccumulator();
  }

  @Override
  public StatsAccumulator addInput(final StatsAccumulator accum, final ActivePowerRecord input) {
    accum.add(input.getValueInW());
    return accum;
  }

  @Override
  public StatsAccumulator mergeAccumulators(final Iterable<StatsAccumulator> accums) {
    final StatsAccumulator merged = this.createAccumulator();
    for (final StatsAccumulator accum : accums) {
      merged.addAll(accum.snapshot());
    }
    return merged;
  }

  @Override
  public Stats extractOutput(final StatsAccumulator accum) {
    return accum.snapshot();
  }
}
