package rocks.theodolite.benchmarks.loadgenerator;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.model.records.ActivePowerRecord;

class EnvVarLoadGeneratorFactory {

  public static final boolean DISABLE_DNS_CACHING_DEFAULT = false;

  private static final Logger LOGGER = LoggerFactory.getLogger(EnvVarLoadGeneratorFactory.class);

  public LoadGenerator create(final LoadGenerator loadGeneratorTemplate) {

    final boolean disableDnsCaching = Boolean.parseBoolean(Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.DISABLE_DNS_CACHING),
        Boolean.toString(DISABLE_DNS_CACHING_DEFAULT)));
    if (disableDnsCaching) {
      this.disableDnsCaching();
    }

    final int numSensors = Integer.parseInt(Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.NUM_SENSORS),
        Integer.toString(LoadGenerator.NUMBER_OF_KEYS_DEFAULT)));
    final int periodMs = Integer.parseInt(Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.PERIOD_MS),
        Integer.toString(LoadGenerator.PERIOD_MS_DEFAULT)));
    final double value = Double.parseDouble(Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.VALUE),
        Integer.toString(LoadGenerator.VALUE_DEFAULT)));
    final int threads = Integer.parseInt(Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.THREADS),
        Integer.toString(LoadGenerator.THREADS_DEFAULT)));

    return loadGeneratorTemplate
        .setClusterConfig(this.buildClusterConfig())
        .setLoadDefinition(new WorkloadDefinition(
            new KeySpace(LoadGenerator.SENSOR_PREFIX_DEFAULT, numSensors),
            Duration.ofMillis(periodMs)))
        .setGeneratorConfig(new LoadGeneratorConfig(
            TitanRecordGenerator.forConstantValue(value),
            this.buildRecordSender()))
        .withThreads(threads);
  }

  private ClusterConfig buildClusterConfig() {
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
      clusterConfig = ClusterConfig.fromBootstrapServer(LoadGenerator.BOOTSTRAP_SERVER_DEFAULT);
      LOGGER.info(
          "Neither a bootstrap server nor a Kubernetes DNS name was provided. Use default bootstrap server '{}'.", // NOCS
          LoadGenerator.BOOTSTRAP_SERVER_DEFAULT);
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

    return clusterConfig;
  }

  private RecordSender<ActivePowerRecord> buildRecordSender() {
    final LoadGeneratorTarget target = LoadGeneratorTarget.from(
        Objects.requireNonNullElse(System.getenv(ConfigurationKeys.TARGET),
            LoadGenerator.TARGET_DEFAULT.getValue()));

    final RecordSender<ActivePowerRecord> recordSender; // NOPMD
    if (target == LoadGeneratorTarget.KAFKA) {
      final String kafkaBootstrapServers = Objects.requireNonNullElse(
          System.getenv(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS),
          LoadGenerator.KAFKA_BOOTSTRAP_SERVERS_DEFAULT);
      final String kafkaInputTopic = Objects.requireNonNullElse(
          System.getenv(ConfigurationKeys.KAFKA_INPUT_TOPIC),
          LoadGenerator.KAFKA_TOPIC_DEFAULT);
      final String schemaRegistryUrl = Objects.requireNonNullElse(
          System.getenv(ConfigurationKeys.SCHEMA_REGISTRY_URL),
          LoadGenerator.SCHEMA_REGISTRY_URL_DEFAULT);
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
      LOGGER.info(
          "Use Kafka as target with bootstrap server '{}', schema registry url '{}' and topic '{}'.", // NOCS
          kafkaBootstrapServers, schemaRegistryUrl, kafkaInputTopic);
    } else if (target == LoadGeneratorTarget.HTTP) {
      final URI url = URI.create(
          Objects.requireNonNullElse(
              System.getenv(ConfigurationKeys.HTTP_URL),
              LoadGenerator.HTTP_URI_DEFAULT));
      recordSender = new HttpRecordSender<>(url);
      LOGGER.info("Use HTTP server as target with url '{}'.", url);
    } else if (target == LoadGeneratorTarget.PUBSUB) {
      final String project = System.getenv(ConfigurationKeys.PUBSUB_PROJECT);
      final String inputTopic = Objects.requireNonNullElse(
          System.getenv(ConfigurationKeys.PUBSUB_INPUT_TOPIC),
          LoadGenerator.PUBSUB_TOPIC_DEFAULT);
      final String emulatorHost = System.getenv(ConfigurationKeys.PUBSUB_EMULATOR_HOST);
      if (emulatorHost != null) { // NOPMD
        LOGGER.info("Use Pub/Sub as target with emulator host {} and topic '{}'.",
            emulatorHost,
            inputTopic);
        recordSender = TitanPubSubSenderFactory.forEmulatedPubSubConfig(emulatorHost, inputTopic);
      } else if (project != null) { // NOPMD
        LOGGER.info("Use Pub/Sub as target with project {} and topic '{}'.", project, inputTopic);
        recordSender = TitanPubSubSenderFactory.forPubSubConfig(project, inputTopic);
      } else {
        throw new IllegalStateException("Neither an emulator host nor  a project was provided.");
      }
    } else {
      // Should never happen
      throw new IllegalStateException("Target " + target + " is not handled yet.");
    }
    return recordSender;
  }

  private void disableDnsCaching() {
    LOGGER.info("Disable DNS caching.");
    java.security.Security.setProperty("networkaddress.cache.ttl", "0");
  }

}
