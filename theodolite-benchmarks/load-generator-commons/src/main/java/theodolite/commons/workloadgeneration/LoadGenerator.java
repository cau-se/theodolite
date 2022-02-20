package theodolite.commons.workloadgeneration;

import java.time.Duration;
import java.util.Objects;

/**
 * A Theodolite load generator.
 */
public final class LoadGenerator {

  public static final String BOOTSTRAP_SERVER_DEFAULT = "localhost:5701";
  public static final String SENSOR_PREFIX_DEFAULT = "s_";
  public static final int NUMBER_OF_KEYS_DEFAULT = 10;
  public static final int PERIOD_MS_DEFAULT = 1000;
  public static final int VALUE_DEFAULT = 10;
  public static final int THREADS_DEFAULT = 4;
  public static final LoadGeneratorTarget TARGET_DEFAULT = LoadGeneratorTarget.KAFKA;
  // Target: HTTP
  public static final String HTTP_URI_DEFAULT = "http://localhost:8080";
  // Target: Kafka
  public static final String SCHEMA_REGISTRY_URL_DEFAULT = "http://localhost:8081";
  public static final String KAFKA_TOPIC_DEFAULT = "input"; // NOCS
  public static final String KAFKA_BOOTSTRAP_SERVERS_DEFAULT = "localhost:9092"; // NOPMD
  // Target: Pub/Sub
  public static final String PUBSUB_TOPIC_DEFAULT = "input"; // NOCS

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
            TitanRecordGenerator.forConstantValue(VALUE_DEFAULT),
            TitanKafkaSenderFactory.forKafkaConfig(
                KAFKA_BOOTSTRAP_SERVERS_DEFAULT,
                KAFKA_TOPIC_DEFAULT,
                SCHEMA_REGISTRY_URL_DEFAULT)));
  }

  /**
   * Create a basic {@link LoadGenerator} from environment variables.
   */
  public static LoadGenerator fromEnvironment() {
    return new EnvVarLoadGeneratorFactory().create(new LoadGenerator());
  }

}
