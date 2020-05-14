package spesb.kafkasender;

import java.util.Properties;
import java.util.function.Function;
import kieker.common.record.IMonitoringRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.common.kieker.kafka.IMonitoringRecordSerde;


/**
 * Sends monitoring records to Kafka.
 *
 * @param <T> {@link IMonitoringRecord} to send
 */
public class KafkaRecordSender<T extends IMonitoringRecord> {

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaRecordSender.class);

  private final String topic;

  private final Function<T, String> keyAccessor;

  private final Function<T, Long> timestampAccessor;

  private final Producer<String, T> producer;

  public KafkaRecordSender(final String bootstrapServers, final String topic) {
    this(bootstrapServers, topic, x -> "", x -> null, new Properties());
  }

  public KafkaRecordSender(final String bootstrapServers, final String topic,
      final Function<T, String> keyAccessor) {
    this(bootstrapServers, topic, keyAccessor, x -> null, new Properties());
  }

  public KafkaRecordSender(final String bootstrapServers, final String topic,
      final Function<T, String> keyAccessor, final Function<T, Long> timestampAccessor) {
    this(bootstrapServers, topic, keyAccessor, timestampAccessor, new Properties());
  }

  /**
   * Create a new {@link KafkaRecordSender}.
   */
  public KafkaRecordSender(final String bootstrapServers, final String topic,
      final Function<T, String> keyAccessor, final Function<T, Long> timestampAccessor,
      final Properties defaultProperties) {
    this.topic = topic;
    this.keyAccessor = keyAccessor;
    this.timestampAccessor = timestampAccessor;

    final Properties properties = new Properties();
    properties.putAll(defaultProperties);
    properties.put("bootstrap.servers", bootstrapServers);
    // properties.put("acks", this.acknowledges);
    // properties.put("batch.size", this.batchSize);
    // properties.put("linger.ms", this.lingerMs);
    // properties.put("buffer.memory", this.bufferMemory);

    this.producer = new KafkaProducer<>(properties, new StringSerializer(),
        IMonitoringRecordSerde.serializer());
  }

  /**
   * Write the passed monitoring record to Kafka.
   */
  public void write(final T monitoringRecord) {
    final ProducerRecord<String, T> record =
        new ProducerRecord<>(this.topic, null, this.timestampAccessor.apply(monitoringRecord),
            this.keyAccessor.apply(monitoringRecord), monitoringRecord);

    LOGGER.debug("Send record to Kafka topic {}: {}", this.topic, record);
    this.producer.send(record);
  }

  public void terminate() {
    this.producer.close();
  }

}
