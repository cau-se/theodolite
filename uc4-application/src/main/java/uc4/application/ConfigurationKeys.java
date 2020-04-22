package uc4.application;

/**
 * Keys to access configuration parameters.
 */
public final class ConfigurationKeys {

  public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";

  public static final String KAFKA_INPUT_TOPIC = "kafka.input.topic";

  public static final String KAFKA_OUTPUT_TOPIC = "kafka.output.topic";

  public static final String AGGREGATION_DURATION_DAYS = "aggregtion.duration.days";

  public static final String AGGREGATION_ADVANCE_DAYS = "aggregtion.advance.days";

  public static final String NUM_THREADS = "num.threads";

  public static final String COMMIT_INTERVAL_MS = "commit.interval.ms";

  public static final String CACHE_MAX_BYTES_BUFFERING = "cache.max.bytes.buffering";

  public static final String KAFKA_WINDOW_DURATION_MINUTES = "kafka.window.duration.minutes";

  private ConfigurationKeys() {}

}
