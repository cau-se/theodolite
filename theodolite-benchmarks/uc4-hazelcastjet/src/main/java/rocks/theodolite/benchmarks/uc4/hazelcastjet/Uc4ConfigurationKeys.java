package rocks.theodolite.benchmarks.uc4.hazelcastjet;

/**
 * Configuration Keys used for Hazelcast Jet Benchmark implementations.
 */
public class Uc4ConfigurationKeys {

  public static final String KAFKA_OUTPUT_TOPIC = "kafka.output.topic";

  public static final String KAFKA_CONFIGURATION_TOPIC = "kafka.configuration.topic";

  public static final String KAFKA_FEEDBACK_TOPIC = "kafka.feedback.topic";

  public static final String EMIT_PERIOD_MS = "emit.period.ms";

  public static final String GRACE_PERIOD_MS = "grace.period.ms";

}
