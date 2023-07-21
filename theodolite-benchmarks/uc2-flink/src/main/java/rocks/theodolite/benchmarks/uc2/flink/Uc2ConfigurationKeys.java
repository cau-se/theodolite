package rocks.theodolite.benchmarks.uc2.flink;

/**
 * Keys to access configuration parameters.
 */
public final class Uc2ConfigurationKeys {

  public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";

  public static final String KAFKA_OUTPUT_TOPIC = "kafka.output.topic";

  public static final String DOWNSAMPLE_INTERVAL_MINUTES = "downsample.interval.minutes";

  private Uc2ConfigurationKeys() {}

}
