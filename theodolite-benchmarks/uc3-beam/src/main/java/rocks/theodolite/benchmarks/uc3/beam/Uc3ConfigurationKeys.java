package rocks.theodolite.benchmarks.uc3.beam;

/**
 * Keys to access configuration parameters.
 */
public final class Uc3ConfigurationKeys {

  public static final String KAFKA_OUTPUT_TOPIC = "kafka.output.topic";

  public static final String AGGREGATION_DURATION_DAYS = "aggregation.duration.days";

  public static final String AGGREGATION_ADVANCE_DAYS = "aggregation.advance.days";

  public static final String TRIGGER_INTERVAL_SECONDS = "trigger.interval";
  // public static final String TRIGGER_INTERVAL_SECONDS = "trigger.interval.seconds";

  private Uc3ConfigurationKeys() {}

}
