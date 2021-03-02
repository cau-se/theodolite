package theodolite.uc4.streamprocessing;

import java.util.Map;
import java.util.Set;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Supplier class for {@link JointFlatTransformerSupplier}.
 */
public class JointFlatTransformerSupplier implements
    TransformerSupplier<String, JointRecordParents, Iterable<KeyValue<SensorParentKey, ActivePowerRecord>>> { // NOCS

  private static final String STORE_NAME = "JOINT-FLAT-MAP-TRANSFORM-STATE";

  @Override
  public Transformer<String, JointRecordParents, Iterable<KeyValue<SensorParentKey, ActivePowerRecord>>> get() { // NOCS
    return new JointFlatTransformer(STORE_NAME);
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
