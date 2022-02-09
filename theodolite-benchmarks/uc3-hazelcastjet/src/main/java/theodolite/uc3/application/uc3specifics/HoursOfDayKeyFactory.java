package theodolite.uc3.application.uc3specifics;

import java.time.LocalDateTime;

/**
 * A factory class to build an {@link HourOfDayKey}.
 *
 */
public class HoursOfDayKeyFactory implements StatsKeyFactory<HourOfDayKey> {

  @Override
  public HourOfDayKey createKey(final String sensorId, final LocalDateTime dateTime) {
    final int hourOfDay = dateTime.getHour();
    return new HourOfDayKey(hourOfDay, sensorId);
  }

  @Override
  public String getSensorId(final HourOfDayKey key) {
    return key.getSensorId();
  }

}
