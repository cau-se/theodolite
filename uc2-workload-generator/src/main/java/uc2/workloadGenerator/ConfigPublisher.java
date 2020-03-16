package titan.ccp.kiekerbridge.expbigdata19;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import titan.ccp.configuration.events.Event;
import titan.ccp.configuration.events.EventSerde;

public class ConfigPublisher {

  private final String topic;

  private final Producer<Event, String> producer;

  public ConfigPublisher(final String bootstrapServers, final String topic) {
    this(bootstrapServers, topic, new Properties());
  }

  public ConfigPublisher(final String bootstrapServers, final String topic,
      final Properties defaultProperties) {
    this.topic = topic;

    final Properties properties = new Properties();
    properties.putAll(defaultProperties);
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "134217728"); // 128 MB
    properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "134217728"); // 128 MB

    this.producer =
        new KafkaProducer<>(properties, EventSerde.serializer(), new StringSerializer());
  }

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
