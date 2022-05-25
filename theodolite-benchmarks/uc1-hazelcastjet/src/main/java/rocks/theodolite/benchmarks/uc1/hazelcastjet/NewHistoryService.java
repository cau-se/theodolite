package rocks.theodolite.benchmarks.uc1.hazelcastjet;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.util.Properties;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.theodolite.benchmarks.commons.hazelcastjet.HazelcastJetService;

/**
 * A microservice that records incoming measurements.
 */
public class NewHistoryService extends HazelcastJetService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryService.class);

  /**
   * Constructs the use case logic for UC1.
   * Retrieves the needed values and instantiates a pipeline factory.
   */
  public NewHistoryService() {
    super(LOGGER);
    final Properties kafkaProps =
        this.propsBuilder.buildReadProperties(
            StringDeserializer.class.getCanonicalName(),
            KafkaAvroDeserializer.class.getCanonicalName());

    this.pipelineFactory = new Uc1PipelineFactory(kafkaProps, this.kafkaInputTopic);

  }

  @Override
  protected void registerSerializer() {
    // empty since we need no serializer in uc1
  }

  public static void main(final String[] args) {
    new NewHistoryService().run();
  }


}
