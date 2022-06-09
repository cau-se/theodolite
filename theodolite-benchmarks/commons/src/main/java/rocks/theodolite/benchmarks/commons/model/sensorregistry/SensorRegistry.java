package rocks.theodolite.benchmarks.commons.model.sensorregistry;

import java.util.Collection;
import java.util.Optional;

/**
 * Hierarchical data structure (i.e. a tree) for organizing sensors. A {@link SensorRegistry} has
 * one top-level sensor which can have multiple child sensors. These sensors can either be real
 * {@link MachineSensor}s or {@link AggregatedSensor}s, having child sensors.
 */
public interface SensorRegistry {

  Optional<MachineSensor> getSensorForIdentifier(final String identifier);

  AggregatedSensor getTopLevelSensor();

  Collection<MachineSensor> getMachineSensors();

  /**
   * Flattens the hierarchy to a collection of all contained {@link Sensor}s.
   *
   * @return A collection of all {@link Sensor}s contained in the hierarchy.
   */
  default Collection<Sensor> flatten() {
    return this.getTopLevelSensor().flatten();
  }

  /**
   * Converts this sensor registry into a json string.
   *
   * <p>
   * Per default a copy of this sensor registry is created to ensure that proper (de)serializers
   * exist. If subclasses have appropriate serdes, they should override this method.
   * </p>
   */
  default String toJson() {
    final ImmutableSensorRegistry immutableSensorRegistry = ImmutableSensorRegistry.copyOf(this);
    return immutableSensorRegistry.toJson();
  }

  static SensorRegistry fromJson(final String json) {
    return ImmutableSensorRegistry.fromJson(json);
  }

}
