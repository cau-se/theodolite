package rocks.theodolite.benchmarks.commons.beam.kafka;

import java.util.Map;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Simple {@link PTransform} that reads from Kafka using {@link KafkaIO}.
 *
 * @param <K> Type of the Key.
 * @param <V> Type of the Value.
 */
public class KafkaGenericReader<K, V> extends PTransform<PBegin, PCollection<KV<K, V>>> {

  private static final long serialVersionUID = 2603286150183186115L;

  private final PTransform<PBegin, PCollection<KV<K, V>>> reader;

  /**
   * Instantiates a {@link PTransform} that reads from Kafka with the given configuration.
   */
  public KafkaGenericReader(
      final String bootstrapServer,
      final String inputTopic,
      final Map<String, Object> consumerConfig,
      final Class<? extends Deserializer<K>> keyDeserializer,
      final Class<? extends Deserializer<V>> valueDeserializer) {
    super();

    // Check if the boostrap server and the input topic are defined
    if (bootstrapServer.isEmpty() || inputTopic.isEmpty()) {
      throw new IllegalArgumentException("bootstrapServer or inputTopic missing");
    }

    this.reader = KafkaIO.<K, V>read()
        .withBootstrapServers(bootstrapServer)
        .withTopic(inputTopic)
        .withKeyDeserializer(keyDeserializer)
        .withValueDeserializer(valueDeserializer)
        .withConsumerConfigUpdates(consumerConfig)
        .withoutMetadata();
  }

  @Override
  public PCollection<KV<K, V>> expand(final PBegin input) {
    return input.apply(this.reader);
  }

}
