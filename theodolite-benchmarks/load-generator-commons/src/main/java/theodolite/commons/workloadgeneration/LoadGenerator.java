package theodolite.commons.workloadgeneration;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * A Theodolite load generator.
 */
public final class LoadGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadGenerator.class);

  private static final String BOOTSTRAP_SERVER_DEFAULT = "localhost:5701";
  private static final String SENSOR_PREFIX_DEFAULT = "s_";
  private static final int NUMBER_OF_KEYS_DEFAULT = 10;
  private static final int PERIOD_MS_DEFAULT = 1000;
  private static final int VALUE_DEFAULT = 10;
  private static final int THREADS_DEFAULT = 4;
  private static final LoadGeneratorTarget TARGET_DEFAULT = LoadGeneratorTarget.KAFKA;
  private static final String SCHEMA_REGISTRY_URL_DEFAULT = "http://localhost:8081";
  private static final String KAFKA_TOPIC_DEFAULT = "input";
  private static final String KAFKA_BOOTSTRAP_SERVERS_DEFAULT = "localhost:9092"; // NOPMD
  private static final String HTTP_URI_DEFAULT = "http://localhost:8080";

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

  public LoadGenerator withKeySpace(final KeySpace keySpace) {
    this.loadDefinition = new WorkloadDefinition(keySpace, this.loadDefinition.getPeriod());
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
        .setClusterConfig(ClusterConfig.fromBootstrapServer(BOOTSTRAP_SERVER_DEFAULT))
        .setLoadDefinition(new WorkloadDefinition(
            new KeySpace(SENSOR_PREFIX_DEFAULT, NUMBER_OF_KEYS_DEFAULT),
            Duration.ofMillis(PERIOD_MS_DEFAULT)))
        .setGeneratorConfig(new LoadGeneratorConfig(
            TitanRecordGeneratorFactory.forConstantValue(VALUE_DEFAULT),
            TitanKafkaSenderFactory.forKafkaConfig(
                KAFKA_BOOTSTRAP_SERVERS_DEFAULT,
                KAFKA_TOPIC_DEFAULT,
                SCHEMA_REGISTRY_URL_DEFAULT)));
  }

  /**
   * Create a basic {@link LoadGenerator} from environment variables.
   */
  public static LoadGenerator fromEnvironment() {
    final String bootstrapServer = System.getenv(ConfigurationKeys.BOOTSTRAP_SERVER);
    final String kubernetesDnsName = System.getenv(ConfigurationKeys.KUBERNETES_DNS_NAME);

    ClusterConfig clusterConfig;
    if (bootstrapServer != null) { // NOPMD
      clusterConfig = ClusterConfig.fromBootstrapServer(bootstrapServer);
      LOGGER.info("Use bootstrap server '{}'.", bootstrapServer);
    } else if (kubernetesDnsName != null) { // NOPMD
      clusterConfig = ClusterConfig.fromKubernetesDnsName(kubernetesDnsName);
      LOGGER.info("Use Kubernetes DNS name '{}'.", kubernetesDnsName);
    } else {
      clusterConfig = ClusterConfig.fromBootstrapServer(BOOTSTRAP_SERVER_DEFAULT);
      LOGGER.info(
          "Neither a bootstrap server nor a Kubernetes DNS name was provided. Use default bootstrap server '{}'.", // NOCS
          BOOTSTRAP_SERVER_DEFAULT);
    }

    final String port = System.getenv(ConfigurationKeys.PORT);
    if (port != null) {
      clusterConfig.setPort(Integer.parseInt(port));
    }

    final String portAutoIncrement = System.getenv(ConfigurationKeys.PORT_AUTO_INCREMENT);
    if (portAutoIncrement != null) {
      clusterConfig.setPortAutoIncrement(Boolean.parseBoolean(portAutoIncrement));
    }

    final String clusterNamePrefix = System.getenv(ConfigurationKeys.CLUSTER_NAME_PREFIX);
    if (clusterNamePrefix != null) {
      clusterConfig.setClusterNamePrefix(portAutoIncrement);
    }

    final LoadGeneratorTarget target = LoadGeneratorTarget.from(
        Objects.requireNonNullElse(System.getenv(ConfigurationKeys.TARGET),
            TARGET_DEFAULT.getValue()));

    final RecordSender<ActivePowerRecord> recordSender; // NOPMD
    if (target == LoadGeneratorTarget.KAFKA) {
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
      recordSender = TitanKafkaSenderFactory.forKafkaConfig(
          kafkaBootstrapServers,
          kafkaInputTopic,
          schemaRegistryUrl);
    } else if (target == LoadGeneratorTarget.HTTP) {
      final URI uri = URI.create(
          Objects.requireNonNullElse(
              System.getenv(ConfigurationKeys.HTTP_URI),
              HTTP_URI_DEFAULT));
      recordSender = new HttpRecordSender<>(uri);
    } else {
      // Should never happen
      throw new IllegalStateException("Target " + target + " is not handled yet.");
    }

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

    return new LoadGenerator()
        .setClusterConfig(clusterConfig)
        .setLoadDefinition(new WorkloadDefinition(
            new KeySpace(SENSOR_PREFIX_DEFAULT, numSensors),
            Duration.ofMillis(periodMs)))
        .setGeneratorConfig(new LoadGeneratorConfig(
            TitanRecordGeneratorFactory.forConstantValue(value),
            recordSender))
        .withThreads(threads);
  }

}
