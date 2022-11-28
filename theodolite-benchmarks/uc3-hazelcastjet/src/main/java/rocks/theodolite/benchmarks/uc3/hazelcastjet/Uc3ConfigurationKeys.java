package rocks.theodolite.benchmarks.uc3.hazelcastjet;

/**
 * Configuration Keys used for Hazelcast Jet Benchmark implementations.
 */
public class Uc3ConfigurationKeys {

  public static final String KAFKA_OUTPUT_TOPIC = "kafka.output.topic";

  public static final String AGGREGATION_DURATION_DAYS = "aggregation.duration.days";

  public static final String AGGREGATION_ADVANCE_DAYS = "aggregation.advance.days";

  public static final String AGGREGATION_EMIT_PERIOD_SECONDS = // NOPMD
      "aggregation.emit.period.seconds";

}
