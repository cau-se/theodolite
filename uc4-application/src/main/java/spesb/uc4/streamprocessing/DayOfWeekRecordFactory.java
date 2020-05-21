package spesb.uc4.streamprocessing;

import com.google.common.math.Stats;
import org.apache.kafka.streams.kstream.Windowed;
import titan.ccp.model.records.DayOfWeekActivePowerRecord;

/**
 * {@link StatsRecordFactory} to create an {@link DayOfWeekActivePowerRecord}.
 */
public class DayOfWeekRecordFactory
    implements StatsRecordFactory<DayOfWeekKey, DayOfWeekActivePowerRecord> {

  @Override
  public DayOfWeekActivePowerRecord create(final Windowed<DayOfWeekKey> windowed,
      final Stats stats) {
    return new DayOfWeekActivePowerRecord(
        windowed.key().getSensorId(),
        windowed.key().getDayOfWeek().getValue(),
        windowed.window().start(),
        windowed.window().end(),
        stats.count(),
        stats.mean(),
        stats.populationVariance(),
        stats.min(),
        stats.max());
  }

}
