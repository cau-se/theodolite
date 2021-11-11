package theodolite.commons.beam;

import java.util.Properties;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import titan.ccp.common.configuration.ServiceConfigurations;

/**
 * Abstraction of a beam microservice.
 */
public class AbstractBeamService {

  // Application Configurations
  public static final Configuration CONFIG = ServiceConfigurations.createWithDefaults();
  public static final String APPLICATION_NAME =
      CONFIG.getString(ConfigurationKeys.APPLICATION_NAME);

  // Beam Pipeline
  protected PipelineOptions options;

  public AbstractBeamService(final String[] args) { //NOPMD
    options = PipelineOptionsFactory.fromArgs(args).create();
    options.setJobName(APPLICATION_NAME);
  }


  /**
   * Abstract main for a Beam Service.
   */
  public static void main(final String[] args){} //NOPMD

  /**
   * Builds a simple configuration for a Kafka consumer.
   *
   * @return the build Kafka consumer configuration.
   */
  public Properties buildConsumerConfig() {
    final Properties consumerConfig = new Properties();
    consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
        CONFIG.getString(ConfigurationKeys.ENABLE_AUTO_COMMIT_CONFIG));
    consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        CONFIG
            .getString(ConfigurationKeys.AUTO_OFFSET_RESET_CONFIG));
    consumerConfig.put("schema.registry.url",
        CONFIG.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL));

    consumerConfig.put("specific.avro.reader",
        CONFIG.getString(ConfigurationKeys.SPECIFIC_AVRO_READER));
    consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, APPLICATION_NAME);
    return consumerConfig;
  }

}
