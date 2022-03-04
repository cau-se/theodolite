package rocks.theodolite.benchmarks.loadgenerator;

import java.util.function.Function;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends records to Kafka.
 *
 * @param <T> Record type to send.
 * @param <K> Internal key type for Kafka records.
 * @param <V> Internal value type for Kafka records.
 */
/* default */ class KafkaRecordSenderImpl<T, K, V> implements KafkaRecordSender<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaRecordSenderImpl.class);

  private final String topic;

  private final Function<T, String> keyAccessor;

  private final Function<T, Long> timestampAccessor;

  private final Producer<K, V> producer;

  private final KafkaRecordFactory<T, K, V> recordFactory;

  /**
   * Create a new {@link KafkaRecordSenderImpl}.
   */
  protected KafkaRecordSenderImpl(
      final Producer<K, V> producer,
      final KafkaRecordFactory<T, K, V> recordFactory,
      final String topic,
      final Function<T, String> keyAccessor,
      final Function<T, Long> timestampAccessor) {
    this.topic = topic;
    this.producer = producer;
    this.recordFactory = recordFactory;
    this.keyAccessor = keyAccessor;
    this.timestampAccessor = timestampAccessor;
  }

  @Override
  public void terminate() {
    this.producer.close();
  }

  @Override
  public void send(final T message) {
    final ProducerRecord<K, V> record = this.recordFactory.create(
        this.topic,
        this.keyAccessor.apply(message),
        message,
        this.timestampAccessor.apply(message));

    LOGGER.debug("Send record to Kafka topic {}: {}", this.topic, record);
    try {
      this.producer.send(record);
    } catch (final SerializationException e) {
      LOGGER.warn(
          "Record could not be serialized and thus not sent to Kafka due to exception. Skipping this record.", // NOCS
          e);
    }
  }

}
