package theodolite.commons.beam.kafka;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;

import java.util.HashMap;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.kafka.common.serialization.StringDeserializer;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Simple {@link PTransform} that read from Kafka using {@link KafkaIO}.
 */
public class KafkaActivePowerRecordReader extends
    PTransform<PBegin, PCollection<KV<String, ActivePowerRecord>>> {

  private static final long serialVersionUID = 2603286150183186115L;
  private final PTransform<PBegin, PCollection<KV<String, ActivePowerRecord>>> reader;


  /**
   * Instantiates a {@link PTransform} that reads from Kafka with the given Configuration.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public KafkaActivePowerRecordReader(final String bootstrapServer, final String inputTopic,
                                      final HashMap consumerConfig) {
    super();

    // Check if boostrap server and inputTopic are defined
    if (bootstrapServer.isEmpty() || inputTopic.isEmpty()) {
      throw new IllegalArgumentException("bootstrapServer or inputTopic missing");
    }

    reader =
        KafkaIO.<String, ActivePowerRecord>read()
            .withBootstrapServers(bootstrapServer)
            .withTopic(inputTopic)
            .withKeyDeserializer(StringDeserializer.class)
            .withValueDeserializerAndCoder((Class) KafkaAvroDeserializer.class,
                AvroCoder.of(ActivePowerRecord.class))
            .withConsumerConfigUpdates(consumerConfig)
            .withoutMetadata();
  }

  @Override
  public PCollection<KV<String, ActivePowerRecord>> expand(final PBegin input) {
    return input.apply(this.reader);
  }

}
