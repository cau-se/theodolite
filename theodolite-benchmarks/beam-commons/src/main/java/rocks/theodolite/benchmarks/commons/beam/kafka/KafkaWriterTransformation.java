package rocks.theodolite.benchmarks.commons.beam.kafka;

import java.util.Map;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * Wrapper for a Kafka writing Transformation where the value type can be generic.
 *
 * @param <T> type of the value.
 */
public class KafkaWriterTransformation<T> extends PTransform<PCollection<KV<String, T>>, PDone> {

  private static final long serialVersionUID = 3171423303843174723L;
  private final PTransform<PCollection<KV<String, T>>, PDone> writer;

  /**
   * Creates a new Kafka writer transformation.
   */
  public KafkaWriterTransformation(
      final String bootstrapServer,
      final String outputTopic,
      final Class<? extends Serializer<T>> valueSerializer) {
    this(bootstrapServer, outputTopic, valueSerializer, Map.of());
  }

  /**
   * Creates a new Kafka writer transformation.
   */
  public KafkaWriterTransformation(
      final String bootstrapServer,
      final String outputTopic,
      final Class<? extends Serializer<T>> valueSerializer,
      final Map<String, Object> producerConfig) {
    super();
    // Check if bootstrap server and outputTopic are defined
    if (bootstrapServer.isEmpty() || outputTopic.isEmpty()) {
      throw new IllegalArgumentException("bootstrapServer or outputTopic missing");
    }

    this.writer = KafkaIO.<String, T>write()
        .withBootstrapServers(bootstrapServer)
        .withTopic(outputTopic)
        .withKeySerializer(StringSerializer.class)
        .withValueSerializer(valueSerializer)
        .withProducerConfigUpdates(producerConfig);

  }

  @Override
  public PDone expand(final PCollection<KV<String, T>> input) {
    return input.apply(this.writer);
  }
}
