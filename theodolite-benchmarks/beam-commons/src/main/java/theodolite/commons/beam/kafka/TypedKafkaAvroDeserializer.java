package theodolite.commons.beam.kafka;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * A Kafka {@link Deserializer} for the Confluent Schema Registry, similar to
 * {@link KafkaAvroDeserializer} but for typed records.
 *
 * @param <T> Type to be deserialized into.
 */
public class TypedKafkaAvroDeserializer<T> implements Deserializer<T> {

  private final KafkaAvroDeserializer deserializer;

  public TypedKafkaAvroDeserializer() {
    this.deserializer = new KafkaAvroDeserializer();
  }

  public TypedKafkaAvroDeserializer(final SchemaRegistryClient client) {
    this.deserializer = new KafkaAvroDeserializer(client);
  }

  public TypedKafkaAvroDeserializer(final SchemaRegistryClient client, final Map<String, ?> props) {
    this.deserializer = new KafkaAvroDeserializer(client, props);
  }

  public TypedKafkaAvroDeserializer(final KafkaAvroDeserializer kafkaAvroDeserializer) {
    this.deserializer = kafkaAvroDeserializer;
  }

  @Override
  public void configure(final Map<String, ?> configs, final boolean isKey) {
    this.deserializer.configure(configs, isKey);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T deserialize(final String s, final byte[] bytes) {
    return (T) this.deserializer.deserialize(s, bytes);
  }

  /**
   * Pass a reader schema to get an Avro projection.
   */
  @SuppressWarnings("unchecked")
  public T deserialize(final String topic, final byte[] bytes, final Schema readerSchema) {
    return (T) this.deserializer.deserialize(topic, bytes, readerSchema);
  }

  @Override
  public void close() {
    this.deserializer.close();
  }

}
