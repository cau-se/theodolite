package rocks.theodolite.benchmarks.commons.hazelcastjet;

import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import titan.ccp.common.configuration.ServiceConfigurations;

public abstract class HazelcastJetService {

  protected final Configuration config = ServiceConfigurations.createWithDefaults();
  protected final String kafkaBootstrapServer;
  protected final String schemaRegistryUrl;
  protected final String jobName;
  protected final String kafkaInputTopic;

  private static final String HZ_KUBERNETES_SERVICE_DNS_KEY = "service-dns";

  protected PipelineFactory pipelineFactory;
  private final JobConfig jobConfig = new JobConfig();

  private Pipeline pipeline;

  JetInstance jetInstance;

  protected final KafkaPropertiesBuilder propsBuilder = new KafkaPropertiesBuilder();


  /**
   * Instantiate a new abstract service.
   */
  public HazelcastJetService(final Logger logger) {
    this.kafkaBootstrapServer = config.getProperty(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS).toString();
    this.jobName = config.getProperty(ConfigurationKeys.APPLICATION_NAME).toString();
    this.schemaRegistryUrl = config.getProperty(ConfigurationKeys.SCHEMA_REGISTRY_URL).toString();
    this.kafkaInputTopic = config.getProperty(ConfigurationKeys.KAFKA_INPUT_TOPIC).toString();


    final JetInstanceBuilder jetInstance = new JetInstanceBuilder()
        .setConfigFromEnv(logger, kafkaBootstrapServer, HZ_KUBERNETES_SERVICE_DNS_KEY);
    this.jetInstance = jetInstance.build();
  }


  /**
   * First initiates a pipeline,
   * Second register the corresponding serializers,
   * Third set the job name,
   * Lastly, Add the job to the hazelcast instance.
   */
  public void run() {
    this.pipeline  = pipelineFactory.buildPipeline();
    registerSerializer();
    jobConfig.setName(config.getString("name"));
    this.jetInstance.newJobIfAbsent(pipeline, jobConfig).join();
  }


  /**
   * Needs to be implemented to register the needed Serializer.
   */
  protected abstract void registerSerializer();

}
