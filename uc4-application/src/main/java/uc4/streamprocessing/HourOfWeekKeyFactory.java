package uc4.streamprocessing;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * {@link StatsKeyFactory} for {@link HourOfWeekKey}.
 */
public class HourOfWeekKeyFactory implements StatsKeyFactory<HourOfWeekKey> {

  @Override
  public HourOfWeekKey createKey(final String sensorId, final LocalDateTime dateTime) {
    final DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
    final int hourOfDay = dateTime.getHour();
    return new HourOfWeekKey(dayOfWeek, hourOfDay, sensorId);
  }

  @Override
  public String getSensorId(final HourOfWeekKey key) {
    return key.getSensorId();
  }

}
