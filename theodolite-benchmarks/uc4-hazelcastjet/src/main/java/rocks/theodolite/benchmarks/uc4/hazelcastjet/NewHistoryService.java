package rocks.theodolite.benchmarks.uc4.hazelcastjet;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.util.Properties;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.theodolite.benchmarks.commons.hazelcastjet.ConfigurationKeys;
import rocks.theodolite.benchmarks.commons.hazelcastjet.HazelcastJetService;
import rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics.EventDeserializer;
import rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics.ImmutableSensorRegistryUc4Serializer;
import rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics.SensorGroupKey;
import rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics.SensorGroupKeySerializer;
import rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics.ValueGroup;
import rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics.ValueGroupSerializer;
import titan.ccp.model.sensorregistry.ImmutableSensorRegistry;


/**
 * A microservice that manages the history and, therefore, stores and aggregates incoming
 * measurements.
 */
public class NewHistoryService extends HazelcastJetService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryService.class);

  /**
   * Constructs the use case logic for UC4.
   * Retrieves the needed values and instantiates a pipeline factory.
   */
  public NewHistoryService() {
    super(LOGGER);
    final Properties kafkaProps =
        this.propsBuilder.buildReadProperties(
            StringDeserializer.class.getCanonicalName(),
            KafkaAvroDeserializer.class.getCanonicalName());

    final Properties kafkaConfigReadProps =
        propsBuilder.buildReadProperties(
            EventDeserializer.class.getCanonicalName(),
            StringDeserializer.class.getCanonicalName());

    final Properties kafkaAggregationReadProps =
        propsBuilder.buildReadProperties(
            StringDeserializer.class.getCanonicalName(),
            KafkaAvroDeserializer.class.getCanonicalName());

    final Properties kafkaWriteProps =
        this.propsBuilder.buildWriteProperties(
            StringSerializer.class.getCanonicalName(),
            KafkaAvroSerializer.class.getCanonicalName());

    final String kafkaOutputTopic =
        config.getProperty(ConfigurationKeys.KAFKA_OUTPUT_TOPIC).toString();

    final String kafkaConfigurationTopic =
        config.getProperty(ConfigurationKeys.KAFKA_CONFIGURATION_TOPIC).toString();

    final String kafkaFeedbackTopic =
        config.getProperty(ConfigurationKeys.KAFKA_FEEDBACK_TOPIC).toString();

    final int windowSize = Integer.parseInt(
        config.getProperty(ConfigurationKeys.WINDOW_SIZE_UC4).toString());

    this.pipelineFactory = new Uc4PipelineFactory(
        kafkaProps,
        kafkaConfigReadProps,
        kafkaAggregationReadProps,
        kafkaWriteProps,
        kafkaInputTopic, kafkaOutputTopic, kafkaConfigurationTopic, kafkaFeedbackTopic,
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
    new NewHistoryService().run();
  }
}
