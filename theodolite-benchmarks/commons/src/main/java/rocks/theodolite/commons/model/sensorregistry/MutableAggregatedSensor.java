package rocks.theodolite.commons.model.sensorregistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A {@link MutableAggregatedSensor} which can child sensors be added to.
 */
public class MutableAggregatedSensor extends AbstractSensor implements AggregatedSensor {

  private final List<Sensor> children = new ArrayList<>();
  private final MutableSensorRegistry sensorRegistry;

  protected MutableAggregatedSensor(final MutableSensorRegistry registry, final String identifier,
      final String name) {
    super(null, identifier, name);
    this.sensorRegistry = registry;
  }

  protected MutableAggregatedSensor(final MutableAggregatedSensor parent, final String identifier,
      final String name) {
    super(parent, identifier, name);
    this.sensorRegistry = parent.sensorRegistry;
  }

  @Override
  public Collection<Sensor> getChildren() {
    return this.children;
  }

  /**
   * Create a new {@link MutableAggregatedSensor} as child of this sensor.
   */
  public MutableAggregatedSensor addChildAggregatedSensor(final String identifier) {
    return this.addChildAggregatedSensor(identifier, "");
  }

  /**
   * Create a new {@link MutableAggregatedSensor} as child of this sensor.
   */
  public MutableAggregatedSensor addChildAggregatedSensor(final String identifier,
      final String name) {
    final MutableAggregatedSensor aggregatedSensor =
        new MutableAggregatedSensor(this, identifier, name);
    this.children.add(aggregatedSensor);
    return aggregatedSensor;
  }

  /**
   * Create a new {@link MachineSensor} as child of this sensor.
   */
  public MachineSensor addChildMachineSensor(final String identifier) {
    return this.addChildMachineSensor(identifier, "");
  }

  /**
   * Create a new {@link MachineSensor} as child of this sensor.
   */
  public MachineSensor addChildMachineSensor(final String identifier, final String name) {
    final MachineSensorImpl machineSensor = new MachineSensorImpl(this, identifier, name);
    final boolean registerResult = this.sensorRegistry.register(machineSensor);
    if (!registerResult) {
      throw new IllegalArgumentException(
          "Sensor width identifier " + identifier + " is already registered.");
    }
    this.children.add(machineSensor);
    return machineSensor;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getIdentifier(), this.children);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof AggregatedSensor) {
      final AggregatedSensor other = (AggregatedSensor) obj;
      return Objects.equals(this.getIdentifier(), other.getIdentifier())
          && Objects.equals(this.children, other.getChildren());
    }
    return false;
  }

  @Override
  public String toString() {
    return this.getName() + '[' + this.getIdentifier() + "] (" + this.children.size()
        + " children)";
  }

}
