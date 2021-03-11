package theodolite.uc2.application;

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
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.common.serialization.Serde;
import theodolite.commons.flink.serialization.FlinkKafkaKeyValueSerde;

/**
 * A class for creating {@link FlinkKafkaConsumer} and {@link FlinkKafkaProducer}.
 */
public class KafkaConnectionFactory {

  private static final Duration PRODUCER_TRANSACTION_TIMEOUT_MS = Duration.ofMinutes(5);

  private final Properties kafkaProps = new Properties();
  private final boolean checkpointingEnabled;
  // private final long checkpointingIntervalMs;
  private final String schemaRegistryUrl;

  public KafkaConnectionFactory(
      final String appName,
      final String bootstrapServers,
      final boolean checkpointingEnabled,
      final String schemaRegistryUrl) {
    this.checkpointingEnabled = checkpointingEnabled;
    this.schemaRegistryUrl = schemaRegistryUrl;
    this.kafkaProps.setProperty("bootstrap.servers", bootstrapServers);
    this.kafkaProps.setProperty("group.id", appName);
  }

  public <T> FlinkKafkaConsumer<T> createConsumer(
      final DeserializationSchema<T> deserializationSchema, final String topic) {
    final FlinkKafkaConsumer<T> consumer =
        new FlinkKafkaConsumer<>(topic, deserializationSchema, this.kafkaProps);
    consumer.setStartFromGroupOffsets();
    if (this.checkpointingEnabled) {
      consumer.setCommitOffsetsOnCheckpoints(true);
    }
    consumer.assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps());
    return consumer;
  }

  // Maybe move to subclass
  public <T extends SpecificRecord> FlinkKafkaConsumer<T> createConsumer(final Class<T> tClass,
      final String topic) {
    final DeserializationSchema<T> deserializationSchema =
        ConfluentRegistryAvroDeserializationSchema.forSpecific(tClass, this.schemaRegistryUrl);
    return this.createConsumer(deserializationSchema, topic);
  }

  public <T> FlinkKafkaProducer<T> createProducer(
      final KafkaSerializationSchema<T> serializationSchema, final String topic) {
    final Properties producerProps = new Properties(this.kafkaProps);
    producerProps.setProperty("transaction.timeout.ms",
        String.valueOf(PRODUCER_TRANSACTION_TIMEOUT_MS.toMillis())); // TODO necessary?
    final FlinkKafkaProducer<T> producer = new FlinkKafkaProducer<>(
        topic, serializationSchema, producerProps, FlinkKafkaProducer.Semantic.AT_LEAST_ONCE);
    producer.setWriteTimestampToKafka(true);
    return producer;
  }

  public <K, V> FlinkKafkaProducer<Tuple2<K, V>> createProducer(
      final Serde<K> kafkaKeySerde, final Serde<V> kafkaValueSerde,
      final TypeInformation<Tuple2<K, V>> typeInformation, final String topic) {
    return this.createProducer(
        new FlinkKafkaKeyValueSerde<>(
            topic,
            () -> kafkaKeySerde,
            () -> kafkaValueSerde,
            typeInformation),
        topic);

  }

}
