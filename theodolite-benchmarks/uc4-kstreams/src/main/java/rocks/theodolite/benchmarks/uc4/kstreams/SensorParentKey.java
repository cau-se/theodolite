package rocks.theodolite.benchmarks.uc4.kstreams;

import java.util.Objects;

/**
 * A key consisting of the identifier of a sensor and an identifier of parent sensor.
 */
public class SensorParentKey {

  private final String sensorIdentifier;

  private final String parentIdentifier;

  public SensorParentKey(final String sensorIdentifier, final String parentIdentifier) {
    this.sensorIdentifier = sensorIdentifier;
    this.parentIdentifier = parentIdentifier;
  }

  public String getSensor() {
    return this.sensorIdentifier;
  }

  public String getParent() {
    return this.parentIdentifier;
  }

  @Override
  public String toString() {
    return "{" + this.sensorIdentifier + ", " + this.parentIdentifier + "}";
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.sensorIdentifier, this.parentIdentifier);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof SensorParentKey) {
      final SensorParentKey other = (SensorParentKey) obj;
      return Objects.equals(this.sensorIdentifier, other.sensorIdentifier)
          && Objects.equals(this.parentIdentifier, other.parentIdentifier);
    }
    return false;
  }

}
