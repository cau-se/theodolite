package rocks.theodolite.benchmarks.commons.hazelcastjet;

import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import java.util.Objects;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.common.configuration.ServiceConfigurations;

/**
 * Abstract HazelcastJetService. Holds common fields and logic shared for all hazelcast jet
 * services. Set common settings and initiates a hazelcast jet instance.
 */
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
   * Instantiate a new abstract service. Retrieves needed fields using ServiceConfiguration and
   * build a new jet instance.
   */
  public HazelcastJetService(final Logger logger) {
    this.jobName = this.config.getString(ConfigurationKeys.APPLICATION_NAME);

    this.kafkaBootstrapServer = this.config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS);
    this.schemaRegistryUrl = this.config.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL);
    this.propsBuilder =
        new KafkaPropertiesBuilder(this.kafkaBootstrapServer, this.schemaRegistryUrl, this.jobName);

    this.kafkaInputTopic = this.config.getString(ConfigurationKeys.KAFKA_INPUT_TOPIC);

    final JetInstanceBuilder jetInstance = new JetInstanceBuilder()
        .setConfigFromEnv(logger, this.kafkaBootstrapServer, HZ_KUBERNETES_SERVICE_DNS_KEY);
    this.jetInstance = jetInstance.build();
  }


  /**
   * Constructs and starts the pipeline. First, initiates a pipeline. Second, register the
   * corresponding serializers. Third, set the job name. Lastly, add the job to the Hazelcast
   * instance.
   */
  public void run() {
    Objects.requireNonNull(this.jobName, "'jobName' must be set.");
    try {
      final Pipeline pipeline = this.pipelineFactory.buildPipeline();
      this.registerSerializer();
      this.jobConfig.setName(this.jobName);
      this.jetInstance.newJobIfAbsent(pipeline, this.jobConfig).join();
    } catch (final Exception e) { // NOPMD
      LOGGER.error("ABORT MISSION!:", e);
    }
  }


  /**
   * Needs to be implemented by subclasses to register the needed Serializer.
   */
  protected abstract void registerSerializer();

}
