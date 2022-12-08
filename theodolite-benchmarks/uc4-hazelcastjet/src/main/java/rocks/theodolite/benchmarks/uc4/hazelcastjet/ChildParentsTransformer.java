package rocks.theodolite.benchmarks.uc4.hazelcastjet;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.AggregatedSensor;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.Sensor;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.SensorRegistry;


/**
 * Transforms a {@link SensorRegistry} into key value pairs of Sensor identifiers and their parents'
 * sensor identifiers. All pairs whose sensor's parents have changed since last iteration are
 * forwarded. A mapping of an identifier to <code>null</code> means that the corresponding sensor
 * does not longer exists in the sensor registry.
 *
 */
public class ChildParentsTransformer {

  public ChildParentsTransformer() {}

  /**
   * Constructs a map of keys to their set of parents out of a SensorRegistry.
   *
   * @param registry The SensorRegistry to build the map out of.
   * @return A map of keys to a set of their parents.
   */
  public Map<String, Set<String>> constructChildParentsPairs(final SensorRegistry registry) {
    return this.streamAllChildren(registry.getTopLevelSensor())
        .collect(Collectors.toMap(
            Sensor::getIdentifier,
            child -> child.getParent()
                .map(p -> Set.of(p.getIdentifier()))
                .orElseGet(Set::of)));
  }

  private Stream<Sensor> streamAllChildren(final AggregatedSensor sensor) {
    return sensor.getChildren().stream()
        .flatMap(s -> Stream.concat(
            Stream.of(s),
            s instanceof AggregatedSensor ? this.streamAllChildren((AggregatedSensor) s)
                : Stream.empty()));
  }

}
