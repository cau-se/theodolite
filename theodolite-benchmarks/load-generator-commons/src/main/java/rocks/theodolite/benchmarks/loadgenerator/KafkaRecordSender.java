package rocks.theodolite.benchmarks.loadgenerator;

import java.util.Properties;
import java.util.function.Function;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import titan.ccp.common.kafka.avro.SchemaRegistryAvroSerdeFactory;

/**
 * Sends records to Kafka.
 *
 * @param <T> Record type to send.
 */
public interface KafkaRecordSender<T> extends RecordSender<T> {

  @Override
  public void close();

  /**
   * Creates a builder object for a {@link KafkaRecordSender} based on a Kafka {@link Serializer}.
   *
   * @param bootstrapServers The server to for accessing Kafka.
   * @param topic The topic where to write.
   * @param serializer The {@link Serializer} for mapping a value to keys.
   */
  public static <T> Builder<T> builderWithSerializer(
      final String bootstrapServers,
      final String topic,
      final Serializer<T> serializer) {
    return new Builder<>(bootstrapServers, topic, serializer);
  }

  /**
   * Creates a Builder object for a {@link KafkaRecordSender} based on a Confluent Schema Registry
   * URL.
   *
   * @param bootstrapServers The Server to for accessing Kafka.
   * @param topic The topic where to write.
   * @param schemaRegistryUrl URL to the schema registry for avro.
   */
  public static <T extends SpecificRecord> Builder<T> builderWithSchemaRegistry(
      final String bootstrapServers,
      final String topic,
      final String schemaRegistryUrl) {
    final SchemaRegistryAvroSerdeFactory avroSerdeFactory =
        new SchemaRegistryAvroSerdeFactory(schemaRegistryUrl);
    return new Builder<>(bootstrapServers, topic, avroSerdeFactory.<T>forValues().serializer());
  }

  /**
   * Builder class to build a new {@link KafkaRecordSenderImpl}.
   *
   * @param <T> Type of the records that should later be send.
   */
  public static class Builder<T> {

    private final String bootstrapServers;
    private final String topic;
    private final Serializer<T> serializer;
    private Function<T, String> keyAccessor = x -> ""; // NOPMD
    private Function<T, Long> timestampAccessor = x -> null; // NOPMD
    private Properties defaultProperties = new Properties(); // NOPMD

    private Builder(final String bootstrapServers, final String topic,
        final Serializer<T> serializer) {
      this.bootstrapServers = bootstrapServers;
      this.topic = topic;
      this.serializer = serializer;
    }

    public Builder<T> keyAccessor(final Function<T, String> keyAccessor) {
      this.keyAccessor = keyAccessor;
      return this;
    }

    public Builder<T> timestampAccessor(final Function<T, Long> timestampAccessor) {
      this.timestampAccessor = timestampAccessor;
      return this;
    }

    public Builder<T> defaultProperties(final Properties defaultProperties) {
      this.defaultProperties = defaultProperties;
      return this;
    }

    /**
     * Create a {@link KafkaRecordSender} from this builder.
     */
    public KafkaRecordSender<T> build() {
      final Properties properties = new Properties();
      properties.putAll(this.defaultProperties);
      properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
      // properties.put("acks", this.acknowledges);
      // properties.put("batch.size", this.batchSize);
      // properties.put("linger.ms", this.lingerMs);
      // properties.put("buffer.memory", this.bufferMemory);

      return new KafkaRecordSenderImpl<>(
          new KafkaProducer<>(
              properties,
              new StringSerializer(),
              this.serializer),
          new DefaultRecordFactory<>(),
          this.topic,
          this.keyAccessor,
          this.timestampAccessor);
    }

    private static class DefaultRecordFactory<T> implements KafkaRecordFactory<T, String, T> {

      @Override
      public ProducerRecord<String, T> create(final String topic, final String key, final T value,
          final long timestamp) {
        return new ProducerRecord<>(topic, null, timestamp, key, value);
      }

    }
  }

  /**
   * Create Kafka {@link ProducerRecord}s from a topic, a key, a value and a timestamp.
   *
   * @param <T> type the records should be created from.
   * @param <K> key type of the {@link ProducerRecord}s.
   * @param <V> value type of the {@link ProducerRecord}s.
   */
  public static interface KafkaRecordFactory<T, K, V> {

    ProducerRecord<K, V> create(String topic, String key, T value, long timestamp);

  }

}
