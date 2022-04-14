package rocks.theodolite.commons.model.sensorregistry;

import java.util.Optional;

abstract class AbstractSensor implements Sensor {

  private final AggregatedSensor parent;

  private final String identifier;

  private final String name;

  protected AbstractSensor(final AggregatedSensor parent, final String identifier,
      final String name) {
    this.parent = parent;
    this.identifier = identifier;
    this.name = name;
  }

  @Override
  public Optional<AggregatedSensor> getParent() {
    return Optional.ofNullable(this.parent);
  }

  @Override
  public String getIdentifier() {
    return this.identifier;
  }

  @Override
  public String getName() {
    return this.name;
  }

}
