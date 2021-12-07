package theodolite.uc2.applicationold;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.aggregate.AggregateOperations;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.jet.pipeline.WindowDefinition;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
//import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * A microservice that manages the history and, therefore, stores and aggregates incoming
 * measurements.
 */
public class HistoryService {
  
  // Don't ask we why constant, because checkstyle
  private static final String BOOTSTRAP_SERVERS = "bootstrap.servers";

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryService.class);
  //static final DateTimeFormatter TIME_FORMATTER_DEFAULT =
  //    DateTimeFormatter.ofPattern("HH:mm:ss:SSS");

  // General Information
  private static final String HZ_KUBERNETES_SERVICE_DNS_KEY = "service-dns";
  private static final String BOOTSTRAP_SERVER_DEFAULT = "localhost:5701";
  private static final String SCHEMA_REGISTRY_URL_DEFAULT = "http://localhost:8081";
  private static final String KAFKA_INPUT_TOPIC_DEFAULT = "input";
  private static final String KAFKA_OUTPUT_TOPIC_DEFAULT = "output";
  private static final String KAFKA_BSERVER_DEFAULT = "localhost:19092";
  // UC2 specific
  private static final String DOWNSAMPLE_INTERVAL_DEFAULT = "5000";

  // Information per History Service
  private ClusterConfig clusterConfig;
  private Properties kafkaReadPropsForPipeline;
  private Properties kafkaWritePropsForPipeline;
  private String kafkaInputTopic;
  private String kafkaOutputTopic;
  // UC2 specific
  private int downsampleInterval;


  /** 
   * Entrypoint for UC2 using Gradle Run. 
   */
  public static void main(final String[] args) {
    HistoryService.loadHistoryService().run();
  }

  /** Build a history service object to run. */
  public static HistoryService loadHistoryService() {
    final String bootstrapServer = System.getenv(ConfigurationKeys.BOOTSTRAP_SERVER);
    final String kubernetesDnsName = System.getenv(ConfigurationKeys.KUBERNETES_DNS_NAME);

    ClusterConfig clusterConfig;
    if (bootstrapServer != null) { // NOPMD
      clusterConfig = ClusterConfig.fromBootstrapServer(bootstrapServer);
      LOGGER.info("Use bootstrap server '{}'.", bootstrapServer);
    } else if (kubernetesDnsName != null) { // NOPMD
      clusterConfig = ClusterConfig.fromKubernetesDnsName(kubernetesDnsName);
      LOGGER.info("Use Kubernetes DNS name '{}'", kubernetesDnsName);
    } else {
      clusterConfig = ClusterConfig.fromBootstrapServer(BOOTSTRAP_SERVER_DEFAULT);
      LOGGER.info(// NOPMD
          "Neitehr a bootstrap server nor a Kubernetes DNS name was provided." 
          + "Use default bootstrap server '{}'",
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
      clusterConfig.setClusterNamePrefix(clusterNamePrefix);
    }

    final String kafkaBootstrapServers = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS),
        KAFKA_BSERVER_DEFAULT);
    final String schemaRegistryUrl = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.SCHEMA_REGISTRY_URL),
        SCHEMA_REGISTRY_URL_DEFAULT);
    final Properties kafkaReadPropsForPipeline =
        buildKafkaReadProps(kafkaBootstrapServers, schemaRegistryUrl);
    final Properties kafkaWritePropsForPipeline =
        buildKafkaWriteProps(kafkaBootstrapServers);

    final String kafkaInputTopic = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_INPUT_TOPIC),
        KAFKA_INPUT_TOPIC_DEFAULT);
    
    final String kafkaOutputTopic = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_OUTPUT_TOPIC),
        KAFKA_OUTPUT_TOPIC_DEFAULT);
    
    final String downsampleInterval = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.DOWNSAMPLE_INTERVAL),
        DOWNSAMPLE_INTERVAL_DEFAULT);
    final int downsampleIntervalNumber = Integer.parseInt(downsampleInterval);

    return new HistoryService()
        .setClusterConfig(clusterConfig)
        .setKafkaReadPropertiesForPipeline(kafkaReadPropsForPipeline)
        .setKafkaWritePropertiesForPipeline(kafkaWritePropsForPipeline)
        .setKafkaInputTopic(kafkaInputTopic)
        .setKafkaOutputTopic(kafkaOutputTopic)
        .setDownsampleInterval(downsampleIntervalNumber);
  }

  /** Set Cluster Config when creating History Service. */
  private HistoryService setClusterConfig(final ClusterConfig clusterConfig) { // NOPMD
    this.clusterConfig = clusterConfig;
    return this;
  }

  /** Set Pipeline Kafka Read Properties. */
  private HistoryService setKafkaReadPropertiesForPipeline(// NOPMD
      final Properties kafkaReadPropertiesForPipeline) {
    this.kafkaReadPropsForPipeline = kafkaReadPropertiesForPipeline;
    return this;
  }
  
  /** Set Pipeline Kafka Write Properties. */
  private HistoryService setKafkaWritePropertiesForPipeline(// NOPMD
      final Properties kafkaWritePropsForPipeline) {
    this.kafkaWritePropsForPipeline = kafkaWritePropsForPipeline;
    return this;
  }

  /** Set Kafka Input topic used to build the pipeline. */
  private HistoryService setKafkaInputTopic(final String kafkaInputTopic) { //NOPMD
    this.kafkaInputTopic = kafkaInputTopic;
    return this;
  }
  
  /** Set Kafka Output topic used to build the pipeline. */
  private HistoryService setKafkaOutputTopic(final String kafkaOutputTopic) { //NOPMD
    this.kafkaOutputTopic = kafkaOutputTopic;
    return this;
  }
  
  /** Set the downsample Interval/Window used in this History Service. */
  private HistoryService setDownsampleInterval(final int downsampleInterval) { //NOPMD
    this.downsampleInterval = downsampleInterval;
    return this;
  }

  /**
   * Defines kafka properties used to fetch data from kafka using a Hazelcast Jet pipeline.
   *
   * @return properties used to fetch data from kafka using a Hazelcast Jet pipeline.
   */
  private static Properties buildKafkaReadProps(final String kafkaBootstrapServer,
      final String schemaRegistryUrl) {
    final Properties props = new Properties();
    props.put(BOOTSTRAP_SERVERS, kafkaBootstrapServer);
    props.put("key.deserializer", StringDeserializer.class.getCanonicalName());
    props.put("value.deserializer", KafkaAvroDeserializer.class);
    props.put("specific.avro.reader", true);
    props.put("schema.registry.url", schemaRegistryUrl);
    props.setProperty("auto.offset.reset", "latest");
    return props;
  }
  
  /**
   * Defines kafka properties used to write data to kafka using a Hazelcast Jet pipeline.
   *
   * @return properties used to fetch data from kafka using a Hazelcast Jet pipeline.
   */
  private static Properties buildKafkaWriteProps(final String kafkaBootstrapServer) {
    final Properties props = new Properties();
    props.put(BOOTSTRAP_SERVERS, kafkaBootstrapServer);
    props.put("key.serializer", StringSerializer.class.getCanonicalName());
    props.put("value.serializer", StringSerializer.class.getCanonicalName());
    return props;
  }

  /**
   * Start the UC2 service.
   */
  public void run() {
    Objects.requireNonNull(this.clusterConfig, "No cluster config set.");
    this.createHazelcastJetApplication();
  }

  /**
   * Build a pipeline and start a Hazelcast Jet Instance and add a job that uses the built pipeline.
   */
  private void createHazelcastJetApplication() {
    
    // Build Pipeline
    final Pipeline pipeline = Pipeline.create();
    final StreamStage<Map.Entry<String, String>> mapProduct =
        pipeline.readFrom(KafkaSources.<String, ActivePowerRecord>kafka(
                this.kafkaReadPropsForPipeline, this.kafkaInputTopic))
            .withNativeTimestamps(0)
            .setLocalParallelism(1)
            .groupingKey(record -> record.getValue().getIdentifier())
            .window(WindowDefinition.tumbling(this.downsampleInterval))
            .aggregate(
                AggregateOperations.averagingDouble(record -> record.getValue().getValueInW()))
            .map(agg -> {
              String theValue = agg.getValue().toString();
              String theKey = agg.getKey().toString();
              return Map.entry(theKey, theValue);
            });
    // Add Sink1: Logger
    mapProduct.writeTo(Sinks.logger());
    // Add Sink2: Write back to kafka for the final benchmark
    mapProduct.writeTo(KafkaSinks.<String, String>kafka(
        this.kafkaWritePropsForPipeline, this.kafkaOutputTopic));
    
    // Set network config for this hazelcast jet instance
    // Create Hazelcast Config
    final Config config = new Config().setClusterName(this.clusterConfig.getClusterNamePrefix());
    final JoinConfig joinConfig = config.getNetworkConfig()
        .setPort(this.clusterConfig.getPort())
        .setPortAutoIncrement(this.clusterConfig.isPortAutoIncrement())
        .getJoin();
    // Set either Bootstrap Server Member or establish Kubernetes Connection
    joinConfig.getMulticastConfig().setEnabled(false);
    if (this.clusterConfig.hasBootstrapServer()) {
      joinConfig.getTcpIpConfig().addMember(this.clusterConfig.getBootstrapServer());
    } else if (this.clusterConfig.hasKubernetesDnsName()) {
      joinConfig.getKubernetesConfig()
        .setEnabled(true)
        .setProperty(HZ_KUBERNETES_SERVICE_DNS_KEY, this.clusterConfig.getKubernetesDnsName());
    }

    // Create Hazelcast Jet Instance
    // Add config and add pipeline as the job
    final JetInstance jet = Jet.newJetInstance();
    jet.getConfig().setHazelcastConfig(config);
    final JobConfig jobConfig = new JobConfig();
    jobConfig.setName("uc2-hazelcastjet");
    jet.newJobIfAbsent(pipeline, jobConfig).join();
  }
  

}
