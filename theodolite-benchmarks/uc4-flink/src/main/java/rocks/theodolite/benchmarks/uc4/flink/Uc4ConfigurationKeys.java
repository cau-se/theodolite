package rocks.theodolite.benchmarks.uc4.flink;

/**
 * Keys to access configuration parameters.
 */
public final class Uc4ConfigurationKeys {

  public static final String CONFIGURATION_KAFKA_TOPIC = "configuration.kafka.topic";

  public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";

  public static final String KAFKA_OUTPUT_TOPIC = "kafka.output.topic";

  public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";

  public static final String EMIT_PERIOD_MS = "emit.period.ms";

  public static final String GRACE_PERIOD_MS = "grace.period.ms";

  private Uc4ConfigurationKeys() {}

}
