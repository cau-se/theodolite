package titan.ccp.aggregation.streamprocessing;

import java.util.Map;
import java.util.Set;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import titan.ccp.models.records.ActivePowerRecord;

/**
 * Factory class configuration required by {@link JointFlatTransformerFactory}.
 */
public class JointFlatTransformerFactory {

  private static final String STORE_NAME = "JOINT-FLAT-MAP-TRANSFORM-STATE";

  /**
   * Returns a {@link TransformerSupplier} for {@link JointFlatTransformer}.
   */
  public TransformerSupplier<String, JointRecordParents, Iterable<KeyValue<SensorParentKey, ActivePowerRecord>>> getTransformerSupplier() { // NOCS
    return new TransformerSupplier<>() {
      @Override
      public JointFlatTransformer get() {
        return new JointFlatTransformer(STORE_NAME);
      }
    };
  }

  /**
   * Returns a {@link StoreBuilder} for {@link JointFlatTransformer}.
   */
  public StoreBuilder<KeyValueStore<String, Set<String>>> getStoreBuilder() {
    return Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(STORE_NAME),
        Serdes.String(),
        ParentsSerde.serde())
        .withLoggingEnabled(Map.of());
  }

  /**
   * Returns the store name for {@link JointFlatTransformer}.
   */
  public String getStoreName() {
    return STORE_NAME;
  }

}
