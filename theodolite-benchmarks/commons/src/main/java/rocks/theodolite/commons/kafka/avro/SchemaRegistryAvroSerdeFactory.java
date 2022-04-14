package rocks.theodolite.commons.kafka.avro;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.util.Collections;
import java.util.Map;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serde;

/**
 * Factory methods to create {@link Serde}s for Avro records using the Confluent Schema Registry.
 */
public final class SchemaRegistryAvroSerdeFactory {

  private static final String SCHEMA_REGISTRY_URL_KEY =
      AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

  private final Map<String, String> serdeConfig;

  public SchemaRegistryAvroSerdeFactory(final String schemaRegistryUrl) {
    this.serdeConfig = Collections.singletonMap(SCHEMA_REGISTRY_URL_KEY, schemaRegistryUrl);

  }

  public <T extends SpecificRecord> Serde<T> forKeys() {
    return this.build(true);
  }

  public <T extends SpecificRecord> Serde<T> forValues() {
    return this.build(false);
  }

  private <T extends SpecificRecord> Serde<T> build(final boolean isKey) {
    final Serde<T> avroSerde = new SpecificAvroSerde<>();
    avroSerde.configure(this.serdeConfig, isKey);
    return avroSerde;
  }

}
