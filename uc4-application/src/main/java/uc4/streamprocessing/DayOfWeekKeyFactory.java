package uc4.streamprocessing;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * {@link StatsKeyFactory} for {@link DayOfWeekKey}.
 */
public class DayOfWeekKeyFactory implements StatsKeyFactory<DayOfWeekKey> {

  @Override
  public DayOfWeekKey createKey(final String sensorId, final LocalDateTime dateTime) {
    final DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
    return new DayOfWeekKey(dayOfWeek, sensorId);
  }

  @Override
  public String getSensorId(final DayOfWeekKey key) {
    return key.getSensorId();
  }

}
