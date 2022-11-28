package rocks.theodolite.benchmarks.commons.beam;

/**
 * Keys to access configuration parameters.
 */
public final class ConfigurationKeys {

  public static final String APPLICATION_NAME = "application.name";

  public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";

  public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";

  public static final String KAFKA_INPUT_TOPIC = "kafka.input.topic";

  public static final String ENABLE_AUTO_COMMIT = "enable.auto.commit";

  public static final String MAX_POLL_RECORDS = "max.poll.records";

  public static final String AUTO_OFFSET_RESET = "auto.offset.reset";

  public static final String SPECIFIC_AVRO_READER = "specific.avro.reader";

  private ConfigurationKeys() {}

}
