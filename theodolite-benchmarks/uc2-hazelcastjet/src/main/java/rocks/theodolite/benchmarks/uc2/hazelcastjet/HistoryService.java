package rocks.theodolite.benchmarks.uc2.hazelcastjet;

import com.google.common.math.StatsAccumulator;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;

import java.time.Duration;
import java.util.Properties;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.theodolite.benchmarks.commons.hazelcastjet.ConfigurationKeys;
import rocks.theodolite.benchmarks.commons.hazelcastjet.HazelcastJetService;
import rocks.theodolite.benchmarks.uc2.hazelcastjet.uc2specifics.StatsAccumulatorSerializer;


/**
 * A microservice that aggregate incoming messages in a tumbling window.
 */
public class HistoryService extends HazelcastJetService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryService.class);


  /**
   * Constructs the use case logic for UC2.
   * Retrieves the needed values and instantiates a pipeline factory.
   */
  public HistoryService() {
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

    final Duration downsampleInterval = Duration.ofMinutes(
        Integer.parseInt(config.getProperty(
            ConfigurationKeys.DOWNSAMPLE_INTERVAL).toString()));

    this.pipelineFactory = new Uc2PipelineFactory(
        kafkaProps,
        this.kafkaInputTopic,
        kafkaWriteProps,
        kafkaOutputTopic,
        downsampleInterval);
  }

  @Override
  protected void registerSerializer() {
    this.jobConfig.registerSerializer(StatsAccumulator.class, StatsAccumulatorSerializer.class);
  }


  public static void main(final String[] args) {
    new HistoryService().run();
  }
}
