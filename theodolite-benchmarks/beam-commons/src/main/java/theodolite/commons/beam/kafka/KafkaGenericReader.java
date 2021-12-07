package theodolite.commons.beam.kafka;

import java.util.Map;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;

/**
 * Simple {@link PTransform} that read from Kafka using {@link KafkaIO}.
 *
 * @param <K> Type of the Key.
 * @param <V> Type of the Value.
 */
public class KafkaGenericReader<K, V> extends
    PTransform<PBegin, PCollection<KV<K, V>>> {

  private static final long serialVersionUID = 2603286150183186115L;
  private final PTransform<PBegin, PCollection<KV<K, V>>> reader;

  /**
   * Instantiates a {@link PTransform} that reads from Kafka with the given Configuration.
   */
  public KafkaGenericReader(final String bootstrapServer, final String inputTopic,
                            final Map<String, Object> consumerConfig,
                            final Class<? extends
                                org.apache.kafka.common.serialization.Deserializer<K>>
                                  keyDeserializer,
                            final Class<? extends
                                org.apache.kafka.common.serialization.Deserializer<V>>
                                  valueDeserializer) {
    super();

    // Check if boostrap server and inputTopic are defined
    if (bootstrapServer.isEmpty() || inputTopic.isEmpty()) {
      throw new IllegalArgumentException("bootstrapServer or inputTopic missing");
    }

    reader =
        KafkaIO.<K, V>read()
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
