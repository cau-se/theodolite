package rocks.theodolite.benchmarks.uc3.beam;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * {@link StatsKeyFactory} for {@link HourOfDayKey}.
 */
public class HourOfDayKeyFactory implements StatsKeyFactory<HourOfDayKey>, Serializable {

  private static final long serialVersionUID = 1L;

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
