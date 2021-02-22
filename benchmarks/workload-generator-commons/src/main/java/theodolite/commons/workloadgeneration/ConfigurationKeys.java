package theodolite.commons.workloadgeneration;

/**
 * Keys to access configuration parameters.
 */
public final class ConfigurationKeys {

  public static final String BOOTSTRAP_SERVER = "BOOTSTRAP_SERVER";

  public static final String PORT = "PORT";

  public static final String PORT_AUTO_INCREMENT = "PORT_AUTO_INCREMENT";

  public static final String CLUSTER_NAME_PREFIX = "CLUSTER_NAME_PREFIX";

  public static final String NUM_SENSORS = "NUM_SENSORS";

  public static final String PERIOD_MS = "PERIOD_MS";

  public static final String VALUE = "VALUE";

  public static final String THREADS = "THREADS";

  public static final String KAFKA_BOOTSTRAP_SERVERS = "KAFKA_BOOTSTRAP_SERVERS";

  public static final String SCHEMA_REGISTRY_URL = "SCHEMA_REGISTRY_URL";

  public static final String KAFKA_INPUT_TOPIC = "KAFKA_INPUT_TOPIC";

  public static final String KAFKA_BATCH_SIZE = "KAFKA_BATCH_SIZE";

  public static final String KAFKA_LINGER_MS = "KAFKA_LINGER_MS";

  public static final String KAFKA_BUFFER_MEMORY = "KAFKA_BUFFER_MEMORY";

  private ConfigurationKeys() {}

}
