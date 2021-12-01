package theodolite.uc3.streamprocessing;

import java.util.Objects;

/**
 * Composed key of an hour of the day and a sensor id.
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
