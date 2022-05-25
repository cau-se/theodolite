package rocks.theodolite.benchmarks.uc3.hazelcastjet;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.util.Properties;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.theodolite.benchmarks.commons.hazelcastjet.ConfigurationKeys;
import rocks.theodolite.benchmarks.commons.hazelcastjet.HazelcastJetService;
import rocks.theodolite.benchmarks.uc3.hazelcastjet.uc3specifics.HourOfDayKey;
import rocks.theodolite.benchmarks.uc3.hazelcastjet.uc3specifics.HourOfDayKeySerializer;

/**
 * A microservice that aggregate incoming messages in a sliding window.
 */
public class NewHistoryService extends HazelcastJetService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryService.class);

  /**
   * Constructs the use case logic for UC3.
   * Retrieves the needed values and instantiates a pipeline factory.
   */
  public NewHistoryService() {
    super(LOGGER);
    final Properties kafkaProps =
        this.propsBuilder.buildReadProperties(
            StringDeserializer.class.getCanonicalName(),
            KafkaAvroDeserializer.class.getCanonicalName());

    final Properties kafkaWriteProps =
        this.propsBuilder.buildWriteProperties(
            StringSerializer.class.getCanonicalName(),
            StringSerializer.class.getCanonicalName());

    final String kafkaOutputTopic =
        config.getProperty(ConfigurationKeys.KAFKA_OUTPUT_TOPIC).toString();

    final int windowSizeInSecondsNumber = Integer.parseInt(
        config.getProperty(ConfigurationKeys.AGGREGATION_DURATION_DAYS).toString());

    final int hoppingSizeInSecondsNumber = Integer.parseInt(
        config.getProperty(ConfigurationKeys.AGGREGATION_ADVANCE_DAYS).toString());

    this.pipelineFactory = new Uc3PipelineFactory(
        kafkaProps,
        kafkaInputTopic,
        kafkaWriteProps,
        kafkaOutputTopic,
        windowSizeInSecondsNumber,
        hoppingSizeInSecondsNumber);
  }

  @Override
  protected void registerSerializer() {
    this.jobConfig.registerSerializer(HourOfDayKey.class, HourOfDayKeySerializer.class);
  }


  public static void main(final String[] args) {
    new NewHistoryService().run();
  }
}
