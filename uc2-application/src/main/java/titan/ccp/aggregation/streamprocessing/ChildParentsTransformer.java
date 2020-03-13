package titan.ccp.aggregation.streamprocessing;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import titan.ccp.configuration.events.Event;
import titan.ccp.model.sensorregistry.AggregatedSensor;
import titan.ccp.model.sensorregistry.Sensor;
import titan.ccp.model.sensorregistry.SensorRegistry;

/**
 * Transforms a {@link SensorRegistry} into key value pairs of Sensor identifiers and their parents'
 * sensor identifiers. All pairs whose sensor's parents have changed since last iteration are
 * forwarded. A mapping of an identifier to <code>null</code> means that the corresponding sensor
 * does not longer exists in the sensor registry.
 */
public class ChildParentsTransformer implements
    Transformer<Event, SensorRegistry, Iterable<KeyValue<String, Optional<Set<String>>>>> {

  private final String stateStoreName;
  // private ProcessorContext context;
  private KeyValueStore<String, Set<String>> state;

  public ChildParentsTransformer(final String stateStoreName) {
    this.stateStoreName = stateStoreName;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void init(final ProcessorContext context) {
    // this.context = context;
    this.state = (KeyValueStore<String, Set<String>>) context.getStateStore(this.stateStoreName);
  }

  @Override
  public Iterable<KeyValue<String, Optional<Set<String>>>> transform(final Event event,
      final SensorRegistry registry) {

    // Values may later be null for deleting a sensor
    final Map<String, Set<String>> childParentsPairs = this.constructChildParentsPairs(registry);

    this.updateChildParentsPairs(childParentsPairs);

    this.updateState(childParentsPairs);

    return childParentsPairs
        .entrySet()
        .stream()
        .map(e -> KeyValue.pair(e.getKey(), Optional.ofNullable(e.getValue())))
        .collect(Collectors.toList());
  }

  @Override
  public void close() {
    // Do nothing
  }

  private Map<String, Set<String>> constructChildParentsPairs(final SensorRegistry registry) {
    return this.streamAllChildren(registry.getTopLevelSensor())
        .collect(Collectors.toMap(
            child -> child.getIdentifier(),
            child -> child.getParent()
                .map(p -> Set.of(p.getIdentifier()))
                .orElseGet(() -> Set.of())));
  }

  private Stream<Sensor> streamAllChildren(final AggregatedSensor sensor) {
    return sensor.getChildren().stream()
        .flatMap(s -> Stream.concat(
            Stream.of(s),
            s instanceof AggregatedSensor ? this.streamAllChildren((AggregatedSensor) s)
                : Stream.empty()));
  }

  private void updateChildParentsPairs(final Map<String, Set<String>> childParentsPairs) {
    final KeyValueIterator<String, Set<String>> oldChildParentsPairs = this.state.all();
    while (oldChildParentsPairs.hasNext()) {
      final KeyValue<String, Set<String>> oldChildParentPair = oldChildParentsPairs.next();
      final String identifier = oldChildParentPair.key;
      final Set<String> oldParents = oldChildParentPair.value;
      final Set<String> newParents = childParentsPairs.get(identifier); // null if not exists
      if (newParents == null) {
        // Sensor was deleted
        childParentsPairs.put(identifier, null);
      } else if (newParents.equals(oldParents)) {
        // No changes
        childParentsPairs.remove(identifier);
      }
      // Else: Later Perhaps: Mark changed parents
    }
    oldChildParentsPairs.close();
  }

  private void updateState(final Map<String, Set<String>> childParentsPairs) {
    for (final Map.Entry<String, Set<String>> childParentPair : childParentsPairs.entrySet()) {
      if (childParentPair.getValue() == null) {
        this.state.delete(childParentPair.getKey());
      } else {
        this.state.put(childParentPair.getKey(), childParentPair.getValue());
      }
    }
  }

}
