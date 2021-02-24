package theodolite.commons.workloadgeneration;

import java.time.Duration;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.clients.producer.ProducerConfig;

/**
 * A Theodolite load generator.
 */
public final class LoadGenerator {

  private static final String SENSOR_PREFIX_DEFAULT = "s_";
  private static final int NUMBER_OF_KEYS_DEFAULT = 10;
  private static final int PERIOD_MS_DEFAULT = 1000;
  private static final int VALUE_DEFAULT = 10;
  private static final int THREADS_DEFAULT = 4;
  private static final String SCHEMA_REGISTRY_URL_DEFAULT = "http://localhost:8081";
  private static final String KAFKA_TOPIC_DEFAULT = "input";
  private static final String KAFKA_BOOTSTRAP_SERVERS_DEFAULT = "localhost:9092"; // NOPMD

  private ClusterConfig clusterConfig;
  private WorkloadDefinition loadDefinition;
  private LoadGeneratorConfig generatorConfig;
  private boolean isStarted;

  private LoadGenerator() {}

  // Add constructor for creating from environment variables

  public LoadGenerator setClusterConfig(final ClusterConfig clusterConfig) { // NOPMD
    this.clusterConfig = clusterConfig;
    return this;
  }

  public LoadGenerator setLoadDefinition(final WorkloadDefinition loadDefinition) { // NOPMD
    this.loadDefinition = loadDefinition;
    return this;
  }

  public LoadGenerator setGeneratorConfig(final LoadGeneratorConfig generatorConfig) { // NOPMD
    this.generatorConfig = generatorConfig;
    return this;
  }

  public LoadGenerator withBeforeAction(final BeforeAction beforeAction) {
    this.generatorConfig.setBeforeAction(beforeAction);
    return this;
  }

  public LoadGenerator withThreads(final int threads) {
    this.generatorConfig.setThreads(threads);
    return this;
  }

  /**
   * Run the constructed load generator until cancellation.
   */
  public void run() {
    Objects.requireNonNull(this.clusterConfig, "No cluster config set.");
    Objects.requireNonNull(this.generatorConfig, "No generator config set.");
    Objects.requireNonNull(this.loadDefinition, "No load definition set.");
    if (this.isStarted) {
      throw new IllegalStateException("Load generator can only be started once.");
    }
    this.isStarted = true;
    final HazelcastRunner runner = new HazelcastRunner(
        this.clusterConfig,
        this.generatorConfig,
        this.loadDefinition);
    runner.runBlocking();
  }

  /**
   * Create a basic {@link LoadGenerator} from its default values.
   */
  public static LoadGenerator fromDefaults() {
    return new LoadGenerator()
        .setClusterConfig(new ClusterConfig())
        .setLoadDefinition(new WorkloadDefinition(
            new KeySpace(SENSOR_PREFIX_DEFAULT, NUMBER_OF_KEYS_DEFAULT),
            Duration.ofMillis(PERIOD_MS_DEFAULT)))
        .setGeneratorConfig(new LoadGeneratorConfig(
            TitanMessageGeneratorFactory
                .withKafkaConfig(
                    KAFKA_BOOTSTRAP_SERVERS_DEFAULT,
                    KAFKA_TOPIC_DEFAULT,
                    SCHEMA_REGISTRY_URL_DEFAULT)
                .forConstantValue(VALUE_DEFAULT)));
  }

  /**
   * Create a basic {@link LoadGenerator} from environment variables.
   */
  public static LoadGenerator fromEnvironment() {
    final String bootstrapServer = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.BOOTSTRAP_SERVER),
        ClusterConfig.BOOTSTRAP_SERVER_DEFAULT);
    final int port = Integer.parseInt(Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.PORT),
        Integer.toString(ClusterConfig.PORT_DEFAULT)));
    final boolean portAutoIncrement = Boolean.parseBoolean(Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.PORT_AUTO_INCREMENT),
        Boolean.toString(ClusterConfig.PORT_AUTO_INCREMENT_DEFAULT)));
    final String clusterNamePrefix = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.CLUSTER_NAME_PREFIX),
        ClusterConfig.CLUSTER_NAME_PREFIX_DEFAULT);
    final int numSensors = Integer.parseInt(Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.NUM_SENSORS),
        Integer.toString(NUMBER_OF_KEYS_DEFAULT)));
    final int periodMs = Integer.parseInt(Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.PERIOD_MS),
        Integer.toString(PERIOD_MS_DEFAULT)));
    final double value = Double.parseDouble(Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.VALUE),
        Integer.toString(VALUE_DEFAULT)));
    final int threads = Integer.parseInt(Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.THREADS),
        Integer.toString(THREADS_DEFAULT)));
    final String kafkaBootstrapServers = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS),
        KAFKA_BOOTSTRAP_SERVERS_DEFAULT);
    final String kafkaInputTopic = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_INPUT_TOPIC),
        KAFKA_TOPIC_DEFAULT);
    final String schemaRegistryUrl = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.SCHEMA_REGISTRY_URL),
        SCHEMA_REGISTRY_URL_DEFAULT);
    final Properties kafkaProperties = new Properties();
    kafkaProperties.compute(ProducerConfig.BATCH_SIZE_CONFIG,
        (k, v) -> System.getenv(ConfigurationKeys.KAFKA_BATCH_SIZE));
    kafkaProperties.compute(ProducerConfig.LINGER_MS_CONFIG,
        (k, v) -> System.getenv(ConfigurationKeys.KAFKA_LINGER_MS));
    kafkaProperties.compute(ProducerConfig.BUFFER_MEMORY_CONFIG,
        (k, v) -> System.getenv(ConfigurationKeys.KAFKA_BUFFER_MEMORY));

    return new LoadGenerator()
        .setClusterConfig(new ClusterConfig(
            bootstrapServer,
            port,
            portAutoIncrement,
            clusterNamePrefix))
        .setLoadDefinition(new WorkloadDefinition(
            new KeySpace(SENSOR_PREFIX_DEFAULT, numSensors),
            Duration.ofMillis(periodMs)))
        .setGeneratorConfig(new LoadGeneratorConfig(
            TitanMessageGeneratorFactory
                .withKafkaConfig(
                    kafkaBootstrapServers,
                    kafkaInputTopic,
                    schemaRegistryUrl,
                    kafkaProperties)
                .forConstantValue(value))
                    .setThreads(threads));
  }

}
