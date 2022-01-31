package theodolite.commons.beam;

import java.util.HashMap;
import java.util.Map;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.clients.consumer.ConsumerConfig;

/**
 * Abstraction of a Beam {@link Pipeline}.
 */
public class AbstractPipeline extends Pipeline {

  private static final String KAFKA_CONFIG_SPECIFIC_AVRO_READER = "specific.avro.reader";
  private static final String KAFKA_CONFIG_SCHEMA_REGISTRY_URL = "schema.registry.url";
  protected final String inputTopic;
  protected final String bootstrapServer;
  // Application Configurations
  private final Configuration config;

  protected AbstractPipeline(final PipelineOptions options, final Configuration config) {
    super(options);
    this.config = config;

    this.inputTopic = config.getString(ConfigurationKeys.KAFKA_INPUT_TOPIC);
    this.bootstrapServer = config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS);
  }

  /**
   * Builds a simple configuration for a Kafka consumer transformation.
   *
   * @return the build configuration.
   */
  public Map<String, Object> buildConsumerConfig() {
    final Map<String, Object> consumerConfig = new HashMap<>();
    consumerConfig.put(
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
        this.config.getString(ConfigurationKeys.ENABLE_AUTO_COMMIT_CONFIG));
    consumerConfig.put(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        this.config.getString(ConfigurationKeys.AUTO_OFFSET_RESET_CONFIG));
    consumerConfig.put(
        KAFKA_CONFIG_SCHEMA_REGISTRY_URL,
        this.config.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL));
    consumerConfig.put(
        KAFKA_CONFIG_SPECIFIC_AVRO_READER,
        this.config.getString(ConfigurationKeys.SPECIFIC_AVRO_READER));
    consumerConfig.put(
        ConsumerConfig.GROUP_ID_CONFIG,
        this.config.getString(ConfigurationKeys.APPLICATION_NAME));
    return consumerConfig;
  }

  /**
   * Builds a simple configuration for a Kafka producer transformation.
   *
   * @return the build configuration.
   */
  public Map<String, Object> buildProducerConfig() {
    final Map<String, Object> config = new HashMap<>();
    config.put(
        KAFKA_CONFIG_SCHEMA_REGISTRY_URL,
        this.config.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL));
    config.put(
        KAFKA_CONFIG_SPECIFIC_AVRO_READER,
        this.config.getString(ConfigurationKeys.SPECIFIC_AVRO_READER));
    return config;
  }
}
