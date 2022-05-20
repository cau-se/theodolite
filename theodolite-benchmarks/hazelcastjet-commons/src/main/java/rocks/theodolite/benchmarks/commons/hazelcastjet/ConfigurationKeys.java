package rocks.theodolite.benchmarks.commons.hazelcastjet;

/**
 * Configuration Keys used for Hazelcast Jet Benchmark implementations.
 */
public class ConfigurationKeys {

  public static final String APPLICATION_NAME = "application.name";

  // Common Keys
  public static final String BOOTSTRAP_SERVER = "BOOTSTRAP_SERVER";
  public static final String KUBERNETES_DNS_NAME = "KUBERNETES_DNS_NAME";
  public static final String PORT = "PORT";
  public static final String PORT_AUTO_INCREMENT = "PORT_AUTO_INCREMENT";
  public static final String CLUSTER_NAME_PREFIX = "CLUSTER_NAME_PREFIX";
  public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";
  public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";
  public static final String KAFKA_INPUT_TOPIC = "kafka.input.topic";

  // Additional topics
  public static final String KAFKA_OUTPUT_TOPIC = "KAFKA_OUTPUT_TOPIC";

  // UC2
  public static final String DOWNSAMPLE_INTERVAL = "DOWNSAMPLE_INTERVAL";

  // UC3
  public static final String WINDOW_SIZE_IN_SECONDS = "WINDOW_SIZE_IN_SECONDS";
  public static final String HOPPING_SIZE_IN_SECONDS = "HOPPING_SIZE_IN_SECONDS";

  // UC4
  public static final String KAFKA_CONFIGURATION_TOPIC = "KAFKA_CONFIGURATION_TOPIC";
  public static final String KAFKA_FEEDBACK_TOPIC = "KAFKA_FEEDBACK_TOPIC";
  public static final String WINDOW_SIZE_UC4 = "WINDOW_SIZE";
  
}
