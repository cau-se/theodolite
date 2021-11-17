package theodolite.commons.beam;

import java.util.Properties;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.clients.consumer.ConsumerConfig;

/**
 * Abstraction of a Beam {@link Pipeline}.
 */
public class AbstractPipeline extends Pipeline {

  // Application Configurations
  private final Configuration config;

  protected AbstractPipeline(final PipelineOptions options, final Configuration config) {
    super(options);
    this.config = config;
  }

  /**
   * Builds a simple configuration for a Kafka consumer transformation.
   *
   * @return the build configuration.
   */
  public Properties buildConsumerConfig() {
    final Properties consumerConfig = new Properties();
    consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
        config.getString(ConfigurationKeys.ENABLE_AUTO_COMMIT_CONFIG));
    consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        config
            .getString(ConfigurationKeys.AUTO_OFFSET_RESET_CONFIG));
    consumerConfig.put("schema.registry.url",
        config.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL));

    consumerConfig.put("specific.avro.reader",
        config.getString(ConfigurationKeys.SPECIFIC_AVRO_READER));

    final String applicationName = config.getString(ConfigurationKeys.APPLICATION_NAME);
    consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, applicationName);
    return consumerConfig;
  }
}
