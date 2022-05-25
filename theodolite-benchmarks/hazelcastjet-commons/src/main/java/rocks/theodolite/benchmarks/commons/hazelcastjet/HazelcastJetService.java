package rocks.theodolite.benchmarks.commons.hazelcastjet;

import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.common.configuration.ServiceConfigurations;

public abstract class HazelcastJetService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastJetService.class);
  private static final String HZ_KUBERNETES_SERVICE_DNS_KEY = "service-dns";

  protected final Configuration config = ServiceConfigurations.createWithDefaults();
  protected final String kafkaBootstrapServer;
  protected final String schemaRegistryUrl;
  protected final String jobName;

  protected final String kafkaInputTopic;

  protected PipelineFactory pipelineFactory;
  protected final JobConfig jobConfig = new JobConfig();
  protected final KafkaPropertiesBuilder propsBuilder;

  private final JetInstance jetInstance;


  /**
   * Instantiate a new abstract service.
   * Retrieves needed fields using ServiceConfiguration and build a new jet instance.
   */
  public HazelcastJetService(final Logger logger) {
    this.jobName = config.getProperty(ConfigurationKeys.APPLICATION_NAME).toString();

    this.kafkaBootstrapServer = config.getProperty(
        ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS).toString();
    this.schemaRegistryUrl = config.getProperty(ConfigurationKeys.SCHEMA_REGISTRY_URL).toString();
    this.propsBuilder =
        new KafkaPropertiesBuilder(kafkaBootstrapServer, schemaRegistryUrl, jobName);

    this.kafkaInputTopic = config.getProperty(ConfigurationKeys.KAFKA_INPUT_TOPIC).toString();

    final JetInstanceBuilder jetInstance = new JetInstanceBuilder()
        .setConfigFromEnv(logger, kafkaBootstrapServer, HZ_KUBERNETES_SERVICE_DNS_KEY);
    this.jetInstance = jetInstance.build();
  }


  /**
   * Constructs and starts the pipeline.
   * First initiates a pipeline,
   * Second register the corresponding serializers,
   * Third set the job name,
   * Lastly, add the job to the hazelcast instance.
   */
  public void run() {
    try {
      final Pipeline pipeline  = pipelineFactory.buildPipeline();
      registerSerializer();
      jobConfig.setName(config.getString("name"));
      this.jetInstance.newJobIfAbsent(pipeline, jobConfig).join();
    } catch (final Exception e) { // NOPMD
      LOGGER.error("ABORT MISSION!:", e);
    }
  }


  /**
   * Needs to be implemented by subclasses to register the needed Serializer.
   */
  protected abstract void registerSerializer();

}
