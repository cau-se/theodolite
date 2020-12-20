package theodolite.uc2.streamprocessing;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import titan.ccp.configuration.events.Event;
import titan.ccp.model.sensorregistry.SensorRegistry;

/**
 * Supplier class for a {@link ChildParentsTransformer}.
 */
public class ChildParentsTransformerSupplier implements
    TransformerSupplier<Event, SensorRegistry, Iterable<KeyValue<String, Optional<Set<String>>>>> {

  private static final String STORE_NAME = "CHILD-PARENTS-TRANSFORM-STATE";

  @Override
  public Transformer<Event, SensorRegistry, Iterable<KeyValue<String, Optional<Set<String>>>>> get() { // NOCS
    return new ChildParentsTransformer(STORE_NAME);
  }

  @Override
  public Set<StoreBuilder<?>> stores() {
    final StoreBuilder<KeyValueStore<String, Set<String>>> store = Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(STORE_NAME),
        Serdes.String(),
        ParentsSerde.serde())
        .withLoggingEnabled(Map.of());

    return Set.of(store);
  }

}
