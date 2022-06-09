package rocks.theodolite.benchmarks.uc4.beam;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import rocks.theodolite.benchmarks.commons.configuration.events.Event;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.AggregatedSensor;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.Sensor;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.SensorRegistry;

/**
 * DoFn class to generate a child-parent pair for every sensor in the hierarchy.
 */
public class GenerateParentsFn extends DoFn<KV<Event, String>, KV<String, Set<String>>> {

  private static final long serialVersionUID = 958270648688932091L;

  /**
   * Transforms a parent [children] map of sensors to a child [parents] map.
   *
   * @param kv input map.
   * @param out outputstream.
   */
  @ProcessElement
  public void processElement(@Element final KV<Event, String> kv,
      final OutputReceiver<KV<String, Set<String>>> out) {
    final Map<String, Set<String>> childParentsPairs =
        this.constructChildParentsPairs(SensorRegistry.fromJson(kv.getValue()));
    final Iterator<Map.Entry<String, Set<String>>> it = childParentsPairs.entrySet().iterator();
    while (it.hasNext()) {
      final Map.Entry<String, Set<String>> pair = it.next();
      out.output(KV.of(pair.getKey(), pair.getValue()));
    }

  }

  private Map<String, Set<String>> constructChildParentsPairs(final SensorRegistry registry) {
    return this.streamAllChildren(registry.getTopLevelSensor())
        .collect(Collectors.<Sensor, String, Set<String>>toMap(
            child -> child.getIdentifier(),
            child -> child.getParent()
                .map(p -> Stream.of(p.getIdentifier()).collect(Collectors.toSet()))
                .orElse(Collections.<String>emptySet())));
  }

  private Stream<Sensor> streamAllChildren(final AggregatedSensor sensor) {
    return sensor.getChildren().stream()
        .flatMap(s -> Stream.concat(
            Stream.of(s),
            s instanceof AggregatedSensor ? this.streamAllChildren((AggregatedSensor) s)
                : Stream.empty()));
  }
}
