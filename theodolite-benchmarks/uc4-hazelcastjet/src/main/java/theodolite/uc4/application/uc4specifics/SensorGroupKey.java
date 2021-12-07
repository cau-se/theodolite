package theodolite.uc4.application.uc4specifics;

import java.util.Objects;

/**
 * Structure (sensorId, group).
 */
public class SensorGroupKey {

  private final String sensorId;
  private final String group;

  public SensorGroupKey(final String sensorId, final String group) {
    this.sensorId = sensorId;
    this.group = group;
  }

  public String getSensorId() {
    return this.sensorId;
  }

  public String getGroup() {
    return this.group;
  }

  @Override
  public String toString() {
    return "[SensorId: " + this.sensorId + "; Group: " + this.group + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.sensorId, this.group);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof SensorGroupKey) {
      final SensorGroupKey other = (SensorGroupKey) obj;
      return Objects.equals(this.sensorId, other.sensorId)
          && Objects.equals(this.group, other.group);
    }
    return false;
  }


}
