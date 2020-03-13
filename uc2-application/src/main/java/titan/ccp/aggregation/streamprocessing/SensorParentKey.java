package titan.ccp.aggregation.streamprocessing;

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

}
