package rocks.theodolite.benchmarks.commons.beam.kafka;

import java.util.Map;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.kafka.common.serialization.StringDeserializer;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;

/**
 * Simple {@link PTransform} that reads from Kafka using {@link KafkaIO} with event time.
 */
public class KafkaActivePowerTimestampReader
    extends PTransform<PBegin, PCollection<KV<String, ActivePowerRecord>>> {

  private static final long serialVersionUID = 2603286150183186115L;
  private final PTransform<PBegin, PCollection<KV<String, ActivePowerRecord>>> reader;

  /**
   * Instantiates a {@link PTransform} that reads from Kafka with the given Configuration.
   */
  public KafkaActivePowerTimestampReader(
      final String bootstrapServer,
      final String inputTopic,
      final Map<String, Object> consumerConfig) {
    super();

    // Check if bootstrap server and inputTopic are defined
    if (bootstrapServer.isEmpty() || inputTopic.isEmpty()) {
      throw new IllegalArgumentException("bootstrapServer or inputTopic missing");
    }

    this.reader = KafkaIO.<String, ActivePowerRecord>read().withBootstrapServers(bootstrapServer)
        .withTopic(inputTopic).withKeyDeserializer(StringDeserializer.class)
        .withValueDeserializerAndCoder(
            ActivePowerRecordDeserializer.class,
            AvroCoder.of(ActivePowerRecord.class))
        .withConsumerConfigUpdates(consumerConfig)
        .withTimestampPolicyFactory(
            (tp, previousWatermark) -> new EventTimePolicy(previousWatermark))
        .withoutMetadata();
  }

  @Override
  public PCollection<KV<String, ActivePowerRecord>> expand(final PBegin input) {
    return input.apply(this.reader);
  }

}
