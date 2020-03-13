package titan.ccp.aggregation;

/**
 * Keys to access configuration parameters.
 */
public final class ConfigurationKeys {

  public static final String CONFIGURATION_KAFKA_TOPIC = "configuration.kafka.topic";

  public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";

  public static final String KAFKA_OUTPUT_TOPIC = "kafka.output.topic";

  public static final String KAFKA_INPUT_TOPIC = "kafka.input.topic";

  public static final String WINDOW_SIZE_MS = "window.size.ms";

  public static final String WINDOW_GRACE_MS = "window.grace.ms";

  public static final String NUM_THREADS = "num.threads";

  public static final String COMMIT_INTERVAL_MS = "commit.interval.ms";

  public static final String CACHE_MAX_BYTES_BUFFERING = "cache.max.bytes.buffering";

  private ConfigurationKeys() {}

}
