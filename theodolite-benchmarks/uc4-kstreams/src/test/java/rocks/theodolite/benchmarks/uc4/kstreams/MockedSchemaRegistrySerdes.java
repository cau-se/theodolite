package rocks.theodolite.benchmarks.uc4.kstreams;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.util.Collections;
import java.util.Map;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serde;
import rocks.theodolite.benchmarks.commons.kafka.avro.SchemaRegistryAvroSerdeFactory;


public class MockedSchemaRegistrySerdes extends SchemaRegistryAvroSerdeFactory {

  private static final String URL_KEY = AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
  private static final String DUMMY_URL = "http://dummy";

  private final MockSchemaRegistryClient schemaRegistryClient = new MockSchemaRegistryClient();
  private final Map<String, String> serdeConfig = Collections.singletonMap(URL_KEY, DUMMY_URL);

  public MockedSchemaRegistrySerdes() {
    super(DUMMY_URL);
  }

  @Override
  public <T extends SpecificRecord> Serde<T> forKeys() {
    return this.build(true);
  }

  @Override
  public <T extends SpecificRecord> Serde<T> forValues() {
    return this.build(false);
  }

  private <T extends SpecificRecord> Serde<T> build(final boolean isKey) {
    final Serde<T> avroSerde = new SpecificAvroSerde<>(this.schemaRegistryClient);
    avroSerde.configure(this.serdeConfig, isKey);
    return avroSerde;
  }

}
