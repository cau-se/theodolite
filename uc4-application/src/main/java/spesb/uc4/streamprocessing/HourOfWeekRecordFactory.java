package spesb.uc4.streamprocessing;

import com.google.common.math.Stats;
import org.apache.kafka.streams.kstream.Windowed;
import titan.ccp.model.records.HourOfWeekActivePowerRecord;

/**
 * {@link StatsRecordFactory} to create an {@link HourOfWeekActivePowerRecord}.
 */
public class HourOfWeekRecordFactory
    implements StatsRecordFactory<HourOfWeekKey, HourOfWeekActivePowerRecord> {

  @Override
  public HourOfWeekActivePowerRecord create(final Windowed<HourOfWeekKey> windowed,
      final Stats stats) {
    return new HourOfWeekActivePowerRecord(
        windowed.key().getSensorId(),
        windowed.key().getDayOfWeek().getValue(),
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
