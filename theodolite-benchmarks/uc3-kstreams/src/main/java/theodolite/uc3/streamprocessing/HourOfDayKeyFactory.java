package theodolite.uc3.streamprocessing;

import java.time.LocalDateTime;

/**
 * {@link StatsKeyFactory} for {@link HourOfDayKey}.
 */
public class HourOfDayKeyFactory implements StatsKeyFactory<HourOfDayKey> {

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
