package rocks.theodolite.benchmarks.uc3.kstreams;

import com.google.common.math.Stats;
import org.apache.kafka.streams.kstream.Windowed;
import rocks.theodolite.benchmarks.commons.model.records.HourOfDayActivePowerRecord;

/**
 * {@link StatsRecordFactory} to create an {@link HourOfDayActivePowerRecord}.
 */
public class HourOfDayRecordFactory
    implements StatsRecordFactory<HourOfDayKey, HourOfDayActivePowerRecord> {

  @Override
  public HourOfDayActivePowerRecord create(final Windowed<HourOfDayKey> windowed,
      final Stats stats) {
    return new HourOfDayActivePowerRecord(
        windowed.key().getSensorId(),
        windowed.key().getHourOfDay(),
        windowed.window().start(),
        windowed.window().end(),
        stats.count(),
        stats.mean(),
        stats.populationVariance(),
        stats.min(),
        stats.max());
  }

}
