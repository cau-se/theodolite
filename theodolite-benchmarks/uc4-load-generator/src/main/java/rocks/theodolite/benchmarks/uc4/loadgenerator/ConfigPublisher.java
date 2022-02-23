package rocks.theodolite.benchmarks.uc4.loadgenerator;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import titan.ccp.configuration.events.Event;
import titan.ccp.configuration.events.EventSerde;

/**
 * Class to publish a configuration to Kafka.
 *
 */
public class ConfigPublisher {

  private static final String MEMORY_CONFIG = "134217728"; // 128 MB

  private final String topic;

  private final Producer<Event, String> producer;

  public ConfigPublisher(final String bootstrapServers, final String topic) {
    this(bootstrapServers, topic, new Properties());
  }

  /**
   * Creates a new {@link ConfigPublisher} object.
   *
   * @param bootstrapServers Zoo Keeper server.
   * @param topic where to write the configuration.
   * @param defaultProperties default properties.
   */
  public ConfigPublisher(final String bootstrapServers, final String topic,
      final Properties defaultProperties) {
    this.topic = topic;

    final Properties properties = new Properties();
    properties.putAll(defaultProperties);
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, MEMORY_CONFIG);
    properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, MEMORY_CONFIG);

    this.producer =
        new KafkaProducer<>(properties, EventSerde.serializer(), new StringSerializer());
  }

  /**
   * Publish an event with given value to the kafka topic.
   *
   * @param event Which {@link Event} happened.
   * @param value Configuration value.
   */
  public void publish(final Event event, final String value) {
    final ProducerRecord<Event, String> record = new ProducerRecord<>(this.topic, event, value);
    try {
      this.producer.send(record).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public void close() {
    this.producer.close();
  }

}
