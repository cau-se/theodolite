package theodolite.uc1.application;

/**
 * Keys to access configuration parameters.
 */
public final class ConfigurationKeys {

  pblic static final String APPLICATION_NAME = "application.name";

  public static final String APPLICATION_VERSION = "application.version";

  public static final String COMMIT_INTERVAL_MS = "commit.interval.ms";

  public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";

  public static final String KAFKA_INPUT_TOPIC = "kafka.input.topic";

  public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";

  public static final String CHECKPOINTING = "checkpointing";

  public static final String PARALLELISM = "parallelism";

  private ConfigurationKeys() {}

}
