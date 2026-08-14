package rocks.theodolite.benchmarks.uc4.commons;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.AggregatedSensor;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.Sensor;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.SensorRegistry;

/**
 * Builds child-to-parent mappings from sensor registries.
 */
public final class ChildParentPairBuilder {

  private ChildParentPairBuilder() {
  }

  /**
   * Builds a mapping from every non-root sensor identifier to its parent identifier.
   *
   * @param registry the sensor registry to traverse
   * @return a mapping from child identifiers to their parent identifiers
   */
  public static Map<String, Set<String>> build(final SensorRegistry registry) {
    return streamAllChildren(registry.getTopLevelSensor())
        .collect(Collectors.toMap(
            Sensor::getIdentifier,
            child -> child.getParent()
                .map(parent -> Set.of(parent.getIdentifier()))
                .orElseGet(Set::of)));
  }

  private static Stream<Sensor> streamAllChildren(final AggregatedSensor sensor) {
    return sensor.getChildren().stream()
        .flatMap(child -> Stream.concat(
            Stream.of(child),
            child instanceof AggregatedSensor
                ? streamAllChildren((AggregatedSensor) child)
                : Stream.empty()));
  }

}