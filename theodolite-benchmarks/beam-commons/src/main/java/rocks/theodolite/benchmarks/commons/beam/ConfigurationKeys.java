package rocks.theodolite.benchmarks.commons.beam;

/**
 * Keys to access configuration parameters.
 */
public final class ConfigurationKeys {
  // Common keys
  public static final String APPLICATION_NAME = "application.name";

  public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";

  public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";

  public static final String KAFKA_INPUT_TOPIC = "kafka.input.topic";

  // Additional topics
  public static final String KAFKA_FEEDBACK_TOPIC = "kafka.feedback.topic";

  public static final String KAFKA_OUTPUT_TOPIC = "kafka.output.topic";

  public static final String KAFKA_CONFIGURATION_TOPIC = "kafka.configuration.topic";

  // UC2
  public static final String DOWNSAMPLE_INTERVAL_MINUTES = "downsample.interval.minutes";

  // UC3
  public static final String AGGREGATION_DURATION_DAYS = "aggregation.duration.days";

  public static final String AGGREGATION_ADVANCE_DAYS = "aggregation.advance.days";

  // UC4
  public static final String EMIT_PERIOD_SECONDS = "kafka.window.duration.minutes";
  // public static final String EMIT_PERIOD_MS = "emit.period.ms";

  public static final String GRACE_PERIOD_MS = "grace.period.ms";

  // BEAM
  public static final String ENABLE_AUTO_COMMIT = "enable.auto.commit";

  public static final String MAX_POLL_RECORDS = "max.poll.records";

  public static final String AUTO_OFFSET_RESET = "auto.offset.reset";

  public static final String SPECIFIC_AVRO_READER = "specific.avro.reader";

  // Used for UC3 + UC4:
  public static final String TRIGGER_INTERVAL_SECONDS = "trigger.interval";
  // public static final String TRIGGER_INTERVAL_SECONDS = "trigger.interval.seconds";


  private ConfigurationKeys() {}

}
