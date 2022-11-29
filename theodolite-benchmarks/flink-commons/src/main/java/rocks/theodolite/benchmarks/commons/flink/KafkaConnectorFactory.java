package rocks.theodolite.benchmarks.commons.flink;

import java.time.Duration;
import java.util.Properties;
import org.apache.avro.specific.SpecificRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serde;
import rocks.theodolite.benchmarks.commons.flink.serialization.FlinkKafkaKeyValueSerde;
import rocks.theodolite.benchmarks.commons.flink.util.SerializableSupplier;

/**
 * A class for creating {@link FlinkKafkaConsumer} and {@link FlinkKafkaProducer}.
 */
public class KafkaConnectorFactory { // NOPMD

  private static final String AUTO_OFFSET_RESET_EARLIEST = "earliest";

  private static final Duration PRODUCER_TRANSACTION_TIMEOUT = Duration.ofMinutes(5);

  private final Properties kafkaProps = new Properties();
  private final boolean checkpointingEnabled;
  private final String schemaRegistryUrl;

  /**
   * Create a new {@link KafkaConnectorFactory} from the provided parameters.
   */
  public KafkaConnectorFactory(
      final String appName,
      final String bootstrapServers,
      final boolean checkpointingEnabled,
      final String schemaRegistryUrl) {
    this.checkpointingEnabled = checkpointingEnabled;
    this.schemaRegistryUrl = schemaRegistryUrl;
    this.kafkaProps.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    this.kafkaProps.setProperty(ConsumerConfig.GROUP_ID_CONFIG, appName);
  }

  /**
   * Create a new {@link FlinkKafkaConsumer} that consumes data using a
   * {@link DeserializationSchema}.
   */
  public <T> FlinkKafkaConsumer<T> createConsumer(final String topic,
      final DeserializationSchema<T> deserializationSchema) {
    return this.createBaseConsumer(
        new FlinkKafkaConsumer<>(topic, deserializationSchema, this.buildConsumerProperties()));
  }

  /**
   * Create a new {@link FlinkKafkaConsumer} that consumes data using a
   * {@link KafkaDeserializationSchema}.
   */
  public <T> FlinkKafkaConsumer<T> createConsumer(final String topic,
      final KafkaDeserializationSchema<T> deserializationSchema) {
    return this.createBaseConsumer(
        new FlinkKafkaConsumer<>(topic, deserializationSchema, this.buildConsumerProperties()));
  }

  /**
   * Create a new {@link FlinkKafkaConsumer} that consumes {@link Tuple2}s using two Kafka
   * {@link Serde}s.
   */
  public <K, V> FlinkKafkaConsumer<Tuple2<K, V>> createConsumer(
      final String topic,
      final SerializableSupplier<Serde<K>> kafkaKeySerde,
      final SerializableSupplier<Serde<V>> kafkaValueSerde,
      final TypeInformation<Tuple2<K, V>> typeInformation) {
    return this.<Tuple2<K, V>>createConsumer(
        topic,
        new FlinkKafkaKeyValueSerde<>(
            topic,
            kafkaKeySerde,
            kafkaValueSerde,
            typeInformation));
  }

  /**
   * Create a new {@link FlinkKafkaConsumer} that consumes from a topic associated with Confluent
   * Schema Registry.
   */
  public <T extends SpecificRecord> FlinkKafkaConsumer<T> createConsumer(final String topic,
      final Class<T> typeClass) {
    // Maybe move to subclass for Confluent-Schema-Registry-specific things
    final DeserializationSchema<T> deserializationSchema =
        ConfluentRegistryAvroDeserializationSchema.forSpecific(typeClass, this.schemaRegistryUrl);
    return this.createConsumer(topic, deserializationSchema);
  }

  private <T> FlinkKafkaConsumer<T> createBaseConsumer(final FlinkKafkaConsumer<T> baseConsumer) {
    baseConsumer.setStartFromGroupOffsets();
    if (this.checkpointingEnabled) {
      baseConsumer.setCommitOffsetsOnCheckpoints(true); // TODO Validate if this is sensible
    }
    baseConsumer.assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps());
    return baseConsumer;
  }


  /**
   * Create a new {@link FlinkKafkaProducer} that produces data using a
   * {@link KafkaSerializationSchema}.
   */
  public <T> FlinkKafkaProducer<T> createProducer(final String topic,
      final KafkaSerializationSchema<T> serializationSchema) {
    final Properties producerProps = this.buildProducerProperties();
    return this.createBaseProducer(new FlinkKafkaProducer<>(
        topic, serializationSchema, producerProps, FlinkKafkaProducer.Semantic.AT_LEAST_ONCE));
  }

  /**
   * Create a new {@link FlinkKafkaProducer} that produces {@link Tuple2}s using two Kafka
   * {@link Serde}s.
   */
  public <K, V> FlinkKafkaProducer<Tuple2<K, V>> createProducer(
      final String topic,
      final SerializableSupplier<Serde<K>> kafkaKeySerde,
      final SerializableSupplier<Serde<V>> kafkaValueSerde,
      final TypeInformation<Tuple2<K, V>> typeInformation) {
    return this.createProducer(
        topic,
        new FlinkKafkaKeyValueSerde<>(
            topic,
            kafkaKeySerde,
            kafkaValueSerde,
            typeInformation));
  }

  private <T> FlinkKafkaProducer<T> createBaseProducer(final FlinkKafkaProducer<T> baseProducer) {
    baseProducer.setWriteTimestampToKafka(true);
    return baseProducer;
  }

  private Properties buildProducerProperties() {
    final Properties producerProps = this.cloneProperties();
    producerProps.setProperty(
        ProducerConfig.TRANSACTION_TIMEOUT_CONFIG,
        String.valueOf(PRODUCER_TRANSACTION_TIMEOUT.toMillis())); // TODO necessary?
    return producerProps;
  }

  private Properties buildConsumerProperties() {
    final Properties consumerProps = this.cloneProperties();
    consumerProps.setProperty(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        AUTO_OFFSET_RESET_EARLIEST);
    return consumerProps;
  }

  private Properties cloneProperties() {
    final Properties props = new Properties();
    props.putAll(this.kafkaProps);
    return props;
  }

}
