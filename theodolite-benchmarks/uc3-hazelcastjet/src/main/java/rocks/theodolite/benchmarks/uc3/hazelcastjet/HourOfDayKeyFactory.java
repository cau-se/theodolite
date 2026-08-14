package rocks.theodolite.benchmarks.uc3.hazelcastjet;

import java.io.Serializable;
import java.time.LocalDateTime;
import rocks.theodolite.benchmarks.uc3.commons.HourOfDayKey;

/**
 * {@link StatsKeyFactory} for {@link HourOfDayKey}.
 */
public class HourOfDayKeyFactory implements StatsKeyFactory<HourOfDayKey>, Serializable {

  private static final long serialVersionUID = 9047643205410220184L;

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
