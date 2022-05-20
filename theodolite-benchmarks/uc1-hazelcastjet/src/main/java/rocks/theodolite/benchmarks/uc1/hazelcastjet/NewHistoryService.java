package rocks.theodolite.benchmarks.uc1.hazelcastjet;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.theodolite.benchmarks.commons.hazelcastjet.HazelcastJetService;

import java.util.Properties;

public class NewHistoryService extends HazelcastJetService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryService.class);

  public NewHistoryService(final Logger logger) {
    super(logger);
    final Properties kafkaProps =
        this.propsBuilder.buildKafkaInputReadPropsFromEnv(this.kafkaBootstrapServer,
            schemaRegistryUrl,
            jobName,
            StringDeserializer.class.getCanonicalName(),
            KafkaAvroDeserializer.class.getCanonicalName());

    this.pipelineFactory = new Uc1PipelineFactory(kafkaProps, this.kafkaInputTopic);

  }


  @Override
  public void run() {
    try {
      super.run();
    } catch (final Exception e) { // NOPMD
      LOGGER.error("ABORT MISSION!: {}", e);
    }
  }

  @Override
  protected void registerSerializer() {
    // empty since we need no serializer in uc1
  }

  public static void main(final String[] args) {
    new NewHistoryService(LOGGER).run();
  }


}
