package rocks.theodolite.benchmarks.uc3.hazelcastjet;

import com.google.common.math.StatsAccumulator;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.time.Duration;
import java.util.Properties;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.theodolite.benchmarks.commons.hazelcastjet.HazelcastJetService;

/**
 * A microservice that aggregate incoming messages in a sliding window.
 */
public class HistoryService extends HazelcastJetService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryService.class);

  /**
   * Constructs the use case logic for UC3. Retrieves the needed values and instantiates a pipeline
   * factory.
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
        this.config.getString(Uc3ConfigurationKeys.KAFKA_OUTPUT_TOPIC);

    final Duration windowSize = Duration.ofDays(
        this.config.getInt(Uc3ConfigurationKeys.AGGREGATION_DURATION_DAYS));

    final Duration hoppingSize = Duration.ofDays(
        this.config.getInt(Uc3ConfigurationKeys.AGGREGATION_ADVANCE_DAYS));

    final Duration emitPeriod = Duration.ofSeconds(
        this.config.getInt(Uc3ConfigurationKeys.AGGREGATION_EMIT_PERIOD_SECONDS));

    this.pipelineFactory = new Uc3PipelineFactory(
        kafkaProps,
        this.kafkaInputTopic,
        kafkaWriteProps,
        kafkaOutputTopic,
        windowSize,
        hoppingSize,
        emitPeriod);
  }

  @Override
  protected void registerSerializer() {
    this.jobConfig.registerSerializer(HourOfDayKey.class, HourOfDayKeySerializer.class);
    this.jobConfig.registerSerializer(StatsAccumulator.class, StatsAccumulatorSerializer.class);
  }

  public static void main(final String[] args) {
    new HistoryService().run();
  }
}
