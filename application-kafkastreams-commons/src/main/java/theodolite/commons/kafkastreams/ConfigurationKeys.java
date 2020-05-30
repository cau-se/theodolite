package theodolite.commons.kafkastreams;

/**
 * Keys to access configuration parameters.
 */
public final class ConfigurationKeys {
  // Common keys
  public static final String APPLICATION_NAME = "application.name";

  public static final String APPLICATION_VERSION = "application.version";

  public static final String NUM_THREADS = "num.threads";

  public static final String COMMIT_INTERVAL_MS = "commit.interval.ms";

  public static final String CACHE_MAX_BYTES_BUFFERING = "cache.max.bytes.buffering";

  public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";

  public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";

  public static final String KAFKA_INPUT_TOPIC = "kafka.input.topic";

  // Additional topics
  public static final String KAFKA_OUTPUT_TOPIC = "kafka.output.topic";

  public static final String CONFIGURATION_KAFKA_TOPIC = "configuration.kafka.topic";

  // UC2
  public static final String WINDOW_SIZE_MS = "window.size.ms";

  public static final String WINDOW_GRACE_MS = "window.grace.ms";

  // UC4
  public static final String AGGREGATION_DURATION_DAYS = "aggregation.duration.days";

  public static final String AGGREGATION_ADVANCE_DAYS = "aggregation.advance.days";


  private ConfigurationKeys() {}

}
