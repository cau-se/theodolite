package rocks.theodolite.benchmarks.uc4.beam;

import java.io.Serializable;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.transforms.Combine.CombineFn;
import titan.ccp.model.records.ActivePowerRecord;
import titan.ccp.model.records.AggregatedActivePowerRecord;



/**
 * CombineFn to aggregate ActivePowerRecords into AggregatedActivePowerRecords.
 */
public class RecordAggregation
    extends CombineFn<ActivePowerRecord, RecordAggregation.Accum, AggregatedActivePowerRecord> {

  private static final long serialVersionUID = 4362213539553233529L;

  /**
   * Wrapper for an accumulation of records.
   */
  @DefaultCoder(AvroCoder.class)
  public static class Accum implements Serializable {
    private static final long serialVersionUID = 3701311203919534376L;
    private long count;
    private Double sum = 0.0;
    private long timestamp;
  }

  @Override
  public Accum createAccumulator() {
    return new Accum();
  }

  @Override
  public Accum addInput(final Accum mutableAccumulator, final ActivePowerRecord input) {
    mutableAccumulator.count += 1;
    mutableAccumulator.sum += input.getValueInW();
    mutableAccumulator.timestamp = input.getTimestamp();
    return mutableAccumulator;
  }

  @Override
  public Accum mergeAccumulators(final Iterable<Accum> accumulators) {
    final Accum merged = this.createAccumulator();
    for (final Accum accumulator : accumulators) {
      merged.count += accumulator.count;
      merged.sum += accumulator.sum;
      merged.timestamp = accumulator.timestamp;
    }

    return merged;
  }

  @Override
  public AggregatedActivePowerRecord extractOutput(final Accum accumulator) {
    final double average = accumulator.count == 0 ? 0.0 : accumulator.sum / accumulator.count;
    return new AggregatedActivePowerRecord("", accumulator.timestamp, accumulator.count,
        accumulator.sum, average);
  }

}
