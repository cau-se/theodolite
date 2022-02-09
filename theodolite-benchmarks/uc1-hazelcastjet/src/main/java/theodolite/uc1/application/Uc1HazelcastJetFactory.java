package theodolite.uc1.application;

import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import java.util.Objects;
import java.util.Properties;
import org.slf4j.Logger;
import theodolite.commons.hazelcastjet.ConfigurationKeys;
import theodolite.commons.hazelcastjet.JetInstanceBuilder;

/**
 * A Hazelcast Jet factory which can build a Hazelcast Jet Instance and Pipeline for the UC1
 * benchmark and lets you start the Hazelcast Jet job. The JetInstance can be built directly as the
 * Hazelcast Config is managed internally. In order to build the Pipeline, you first have to build
 * the Properties and set the input topic which can be done using internal functions of this
 * factory. Outside data only refers to custom values or default values in case data of the
 * environment cannot the fetched.
 */
public class Uc1HazelcastJetFactory {

  // Information per History Service
  private Properties kafkaPropertiesForPipeline;
  private String kafkaInputTopic;
  private JetInstance uc1JetInstance;
  private Pipeline uc1JetPipeline;

  /////////////////////////////////////
  // Layer 1 - Hazelcast Jet Run Job //
  /////////////////////////////////////

  /**
   * Needs a JetInstance and Pipeline defined in this factors. Adds the pipeline to the existing
   * JetInstance as a job.
   *
   * @param jobName The name of the job.
   */
  public void runUc1Job(final String jobName) {

    // Check if a Jet Instance for UC1 is set.
    if (this.uc1JetInstance == null) {
      throw new IllegalStateException("Jet Instance is not set! "
          + "Cannot start a hazelcast jet job for UC1.");
    }

    // Check if a Pipeline for UC1 is set.
    if (this.uc1JetPipeline == null) {
      throw new IllegalStateException(
          "Hazelcast Pipeline is not set! Cannot start a hazelcast jet job for UC1.");
    }

    // Adds the job name and joins a job to the JetInstance defined in this factory
    final JobConfig jobConfig = new JobConfig();
    jobConfig.setName(jobName);
    this.uc1JetInstance.newJobIfAbsent(this.uc1JetPipeline, jobConfig).join();
  }

  /////////////
  // Layer 2 //
  /////////////

  /**
   * Build a Hazelcast JetInstance used to run a job on.
   *
   * @param logger The logger specified for this JetInstance.
   * @param bootstrapServerDefault Default bootstrap server in case no value can be derived from the
   *        environment.
   * @param hzKubernetesServiceDnsKey The kubernetes service dns key.
   * @return A Uc1HazelcastJetFactory containing a set JetInstance.
   */
  public Uc1HazelcastJetFactory buildUc1JetInstanceFromEnv(final Logger logger,
      final String bootstrapServerDefault,
      final String hzKubernetesServiceDnsKey) {
    this.uc1JetInstance = new JetInstanceBuilder()
        .setConfigFromEnv(logger, bootstrapServerDefault, hzKubernetesServiceDnsKey)
        .build();
    return this;
  }

  /**
   * Builds a Hazelcast Jet pipeline used for a JetInstance to run it as a job on. Needs the input
   * topic and kafka properties defined in this factory beforehand.
   *
   * @return A Uc1HazelcastJetFactory containg a set pipeline.
   */
  public Uc1HazelcastJetFactory buildUc1Pipeline() {

    // Check if Properties for the Kafka Input are set.
    if (this.kafkaPropertiesForPipeline == null) {
      throw new IllegalStateException(
          "Kafka Properties for pipeline not set! Cannot build pipeline.");
    }

    // Check if the Kafka input topic is set.
    if (this.kafkaInputTopic == null) {
      throw new IllegalStateException("Kafka input topic for pipeline not set! "
          + "Cannot build pipeline.");
    }

    // Build Pipeline Using the pipelineBuilder
    final Uc1PipelineBuilder pipeBuilder = new Uc1PipelineBuilder();
    this.uc1JetPipeline =
        pipeBuilder.build(this.kafkaPropertiesForPipeline, this.kafkaInputTopic);
    // Return Uc1HazelcastJetBuilder factory
    return this;
  }

  /////////////
  // Layer 3 //
  /////////////

  /**
   * Sets kafka properties for pipeline used in this builder.
   *
   * @param kafkaProperties A propeties object containing necessary values used for the hazelcst jet
   *        kafka connection.
   * @return The Uc1HazelcastJetBuilder factory with set kafkaPropertiesForPipeline.
   */
  public Uc1HazelcastJetFactory setCustomProperties(final Properties kafkaProperties) { // NOPMD
    this.kafkaPropertiesForPipeline = kafkaProperties;
    return this;
  }

  /**
   * Sets kafka properties for pipeline used in this builder using environment variables.
   *
   * @param bootstrapServersDefault Default Bootstrap server in the case that no bootstrap server
   *        can be fetched from the environment.
   * @param schemaRegistryUrlDefault Default schema registry url in the case that no schema registry
   *        url can be fetched from the environment.
   * @return The Uc1HazelcastJetBuilder factory with set kafkaPropertiesForPipeline.
   */
  public Uc1HazelcastJetFactory setPropertiesFromEnv(final String bootstrapServersDefault, // NOPMD
      final String schemaRegistryUrlDefault) {
    // Use KafkaPropertiesBuilder to build a properties object used for kafka
    final Uc1KafkaPropertiesBuilder propsBuilder = new Uc1KafkaPropertiesBuilder();
    final Properties kafkaProps =
        propsBuilder.buildKafkaPropsFromEnv(bootstrapServersDefault,
            schemaRegistryUrlDefault);
    this.kafkaPropertiesForPipeline = kafkaProps;
    return this;
  }

  /**
   * Sets the kafka input topic for the pipeline used in this builder.
   *
   * @param inputTopic The kafka topic used as the pipeline input.
   * @return A Uc1HazelcastJetBuilder factory with a set kafkaInputTopic.
   */
  public Uc1HazelcastJetFactory setCustomKafkaInputTopic(final String inputTopic) { // NOPMD
    this.kafkaInputTopic = inputTopic;
    return this;
  }

  /**
   * Sets the kafka input topic for the pipeline used in this builder using environment variables.
   *
   * @param defaultInputTopic The default kafka input topic used if no topic is specified by the
   *        environment.
   * @return A Uc1HazelcastJetBuilder factory with a set kafkaInputTopic.
   */
  public Uc1HazelcastJetFactory setKafkaInputTopicFromEnv(final String defaultInputTopic) { // NOPMD
    this.kafkaInputTopic = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_INPUT_TOPIC),
        defaultInputTopic);
    return this;
  }



}
