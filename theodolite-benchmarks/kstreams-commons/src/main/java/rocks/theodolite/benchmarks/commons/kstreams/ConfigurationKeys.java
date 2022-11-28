package rocks.theodolite.benchmarks.commons.kstreams;

/**
 * Keys to access configuration parameters.
 */
public final class ConfigurationKeys {
  // Common keys
  public static final String APPLICATION_NAME = "application.name";

  public static final String APPLICATION_VERSION = "application.version";

  public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";

  public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";

  public static final String KAFKA_INPUT_TOPIC = "kafka.input.topic";

  private ConfigurationKeys() {}

}
