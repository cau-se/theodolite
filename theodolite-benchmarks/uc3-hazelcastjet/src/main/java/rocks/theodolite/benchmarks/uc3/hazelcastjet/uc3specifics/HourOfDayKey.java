package rocks.theodolite.benchmarks.uc3.hazelcastjet.uc3specifics;

import java.util.Objects;

/**
 * A key consisting of a hour of a day and a sensorID.
 *
 */
public class HourOfDayKey {

  private final int hourOfDay;
  private final String sensorId;

  public HourOfDayKey(final int hourOfDay, final String sensorId) {
    this.hourOfDay = hourOfDay;
    this.sensorId = sensorId;
  }

  public int getHourOfDay() {
    return this.hourOfDay;
  }

  public String getSensorId() {
    return this.sensorId;
  }

  @Override
  public String toString() {
    return this.sensorId + ";" + this.hourOfDay;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.hourOfDay, this.sensorId);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof HourOfDayKey) {
      final HourOfDayKey other = (HourOfDayKey) obj;
      return Objects.equals(this.hourOfDay, other.hourOfDay)
          && Objects.equals(this.sensorId, other.sensorId);
    }
    return false;
  }

}
