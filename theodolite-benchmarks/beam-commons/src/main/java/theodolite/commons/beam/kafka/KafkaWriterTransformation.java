package theodolite.commons.beam.kafka;

import java.util.Properties;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;


public class KafkaWriterTransformation<K> extends
    PTransform<PCollection<KV<String, K>>, PDone> {

  private static final long serialVersionUID = 3171423303843174723L;
  private final PTransform<PCollection<KV<String, K>>, PDone> writer;

  public KafkaWriterTransformation(final String bootstrapServer, final String outputTopic,
                                   final java.lang
                                       .Class<? extends org.apache.kafka
                                       .common.serialization
                                       .Serializer<K>> valueSerializer) {
    super();
    // Check if boostrap server and outputTopic are defined
    if (bootstrapServer.isEmpty() || outputTopic.isEmpty()) {
      throw new IllegalArgumentException("bootstrapServer or outputTopic missing");
    }

    this.writer = KafkaIO.<String, K>write()
        .withBootstrapServers(bootstrapServer)
        .withTopic(outputTopic)
        .withKeySerializer(StringSerializer.class)
        .withValueSerializer(valueSerializer);

  }

  @Override
  public PDone expand(PCollection<KV<String, K>> input) {
    return input.apply(this.writer);
  }
}
