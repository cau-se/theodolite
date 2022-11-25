package rocks.theodolite.benchmarks.uc3.flink;

/**
 * Keys to access configuration parameters.
 */
public final class ConfigurationKeys {

  public static final String APPLICATION_NAME = "application.name";

  public static final String APPLICATION_VERSION = "application.version";

  public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";

  public static final String KAFKA_INPUT_TOPIC = "kafka.input.topic";

  public static final String KAFKA_OUTPUT_TOPIC = "kafka.output.topic";

  public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";

  public static final String AGGREGATION_DURATION_DAYS = "aggregation.duration.days";

  public static final String AGGREGATION_ADVANCE_DAYS = "aggregation.advance.days";

  public static final String AGGREGATION_TRIGGER_INTERVAL_SECONDS = // NOPMD
      "aggregation.trigger.interval.seconds";

  public static final String COMMIT_INTERVAL_MS = "commit.interval.ms";

  public static final String TIME_ZONE = "time.zone";

  public static final String FLINK_STATE_BACKEND = "flink.state.backend";

  public static final String FLINK_STATE_BACKEND_PATH = "flink.state.backend.path";

  public static final String FLINK_STATE_BACKEND_MEMORY_SIZE = // NOPMD
      "flink.state.backend.memory.size";

  public static final String CHECKPOINTING = "checkpointing";

  public static final String PARALLELISM = "parallelism";

  private ConfigurationKeys() {}

}
