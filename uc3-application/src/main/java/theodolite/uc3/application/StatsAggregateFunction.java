package theodolite.uc3.application;

import com.google.common.math.Stats;
import com.google.common.math.StatsAccumulator;
import org.apache.flink.api.common.functions.AggregateFunction;
import theodolite.uc3.application.util.StatsFactory;
import titan.ccp.models.records.ActivePowerRecord;

/**
 * Statistical aggregation of {@link ActivePowerRecord}s using {@link Stats}.
 */
@SuppressWarnings("UnstableApiUsage")
public class StatsAggregateFunction implements AggregateFunction<ActivePowerRecord, Stats, Stats> {

  private static final long serialVersionUID = -8873572990921515499L;

  @Override
  public Stats createAccumulator() {
    return Stats.of();
  }

  @Override
  public Stats add(final ActivePowerRecord value, final Stats accumulator) {
    return StatsFactory.accumulate(accumulator, value.getValueInW());
  }

  @Override
  public Stats getResult(final Stats accumulator) {
    return accumulator;
  }

  @Override
  public Stats merge(final Stats a, final Stats b) {
    final StatsAccumulator statsAccumulator = new StatsAccumulator();
    statsAccumulator.addAll(a);
    statsAccumulator.addAll(b);
    return statsAccumulator.snapshot();
  }
}
