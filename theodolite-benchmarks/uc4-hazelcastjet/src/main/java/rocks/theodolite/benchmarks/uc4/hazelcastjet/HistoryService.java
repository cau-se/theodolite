package rocks.theodolite.benchmarks.uc4.hazelcastjet;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.time.Duration;
import java.util.Properties;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.theodolite.benchmarks.commons.hazelcastjet.HazelcastJetService;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.ImmutableSensorRegistry;


/**
 * A microservice that manages the history and, therefore, stores and aggregates incoming
 * measurements.
 */
public class HistoryService extends HazelcastJetService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryService.class);

  /**
   * Constructs the use case logic for UC4. Retrieves the needed values and instantiates a pipeline
   * factory.
   */
  public HistoryService() {
    super(LOGGER);
    final Properties kafkaProps =
        this.propsBuilder.buildReadProperties(
            StringDeserializer.class.getCanonicalName(),
            KafkaAvroDeserializer.class.getCanonicalName());

    final Properties kafkaConfigReadProps =
        this.propsBuilder.buildReadProperties(
            EventDeserializer.class.getCanonicalName(),
            StringDeserializer.class.getCanonicalName());

    final Properties kafkaAggregationReadProps =
        this.propsBuilder.buildReadProperties(
            StringDeserializer.class.getCanonicalName(),
            KafkaAvroDeserializer.class.getCanonicalName());

    final Properties kafkaWriteProps =
        this.propsBuilder.buildWriteProperties(
            StringSerializer.class.getCanonicalName(),
            KafkaAvroSerializer.class.getCanonicalName());

    final String outputTopic = this.config.getString(Uc4ConfigurationKeys.KAFKA_OUTPUT_TOPIC);

    final String configurationTopic =
        this.config.getString(Uc4ConfigurationKeys.KAFKA_CONFIGURATION_TOPIC);

    final String feedbackTopic = this.config.getString(Uc4ConfigurationKeys.KAFKA_FEEDBACK_TOPIC);

    final Duration windowSize = Duration.ofMillis(
        this.config.getInt(Uc4ConfigurationKeys.EMIT_PERIOD_MS));

    this.pipelineFactory = new Uc4PipelineFactory(
        kafkaProps,
        kafkaConfigReadProps,
        kafkaAggregationReadProps,
        kafkaWriteProps,
        this.kafkaInputTopic, outputTopic, configurationTopic, feedbackTopic,
        windowSize);
  }

  @Override
  protected void registerSerializer() {
    this.jobConfig.registerSerializer(ValueGroup.class, ValueGroupSerializer.class)
        .registerSerializer(SensorGroupKey.class, SensorGroupKeySerializer.class)
        .registerSerializer(ImmutableSensorRegistry.class,
            ImmutableSensorRegistryUc4Serializer.class);
  }


  public static void main(final String[] args) {
    new HistoryService().run();
  }
}
