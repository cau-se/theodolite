package rocks.theodolite.benchmarks.uc2.flink;

import com.google.common.math.Stats;
import com.google.common.math.StatsAccumulator;
import org.apache.flink.api.common.functions.AggregateFunction;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;

/**
 * Statistical aggregation of {@link ActivePowerRecord}s using {@link Stats}.
 */
public class StatsAggregateFunction
    implements AggregateFunction<ActivePowerRecord, StatsAccumulator, Stats> {

  private static final long serialVersionUID = -8873572990921515499L; // NOPMD

  @Override
  public StatsAccumulator createAccumulator() {
    return new StatsAccumulator();
  }

  @Override
  public StatsAccumulator add(final ActivePowerRecord value, final StatsAccumulator accumulator) {
    accumulator.add(value.getValueInW());
    return accumulator;
  }

  @Override
  public Stats getResult(final StatsAccumulator accumulator) {
    return accumulator.snapshot();
  }

  @Override
  public StatsAccumulator merge(final StatsAccumulator a, final StatsAccumulator b) {
    a.addAll(b.snapshot());
    return a;
  }
}
