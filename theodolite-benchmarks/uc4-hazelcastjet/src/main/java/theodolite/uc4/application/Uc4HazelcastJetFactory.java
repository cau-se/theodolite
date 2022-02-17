package theodolite.uc4.application;

import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import java.util.Objects;
import java.util.Properties;
import org.slf4j.Logger;
import theodolite.commons.hazelcastjet.ConfigurationKeys;
import theodolite.commons.hazelcastjet.JetInstanceBuilder;
import theodolite.uc4.application.uc4specifics.ImmutableSensorRegistryUc4Serializer;
import theodolite.uc4.application.uc4specifics.SensorGroupKey;
import theodolite.uc4.application.uc4specifics.SensorGroupKeySerializer;
import theodolite.uc4.application.uc4specifics.ValueGroup;
import theodolite.uc4.application.uc4specifics.ValueGroupSerializer;
import titan.ccp.model.sensorregistry.ImmutableSensorRegistry;

/**
 * A Hazelcast Jet factory which can build a Hazelcast Jet Instance and Pipeline for the UC4
 * benchmark and lets you start the Hazelcast Jet job. The JetInstance can be built directly as the
 * Hazelcast Config is managed internally. In order to build the Pipeline, you first have to build
 * the Read and Write Propertiesand set the input, output, and configuration topic. This can be done
 * using internal functions of this factory. Outside data only refers to custom values or default
 * values in case data of the environment cannot the fetched.
 */
public class Uc4HazelcastJetFactory {

  // Information per History Service
  private Properties kafkaInputReadPropsForPipeline;
  private Properties kafkaConfigPropsForPipeline;
  private Properties kafkaFeedbackPropsForPipeline;
  private Properties kafkaWritePropsForPipeline;
  private String kafkaInputTopic;
  private String kafkaOutputTopic;
  private JetInstance uc4JetInstance;
  private Pipeline uc4JetPipeline;
  // UC4 specific
  private String kafkaConfigurationTopic;
  private String kafkaFeedbackTopic;
  private int windowSize;

  /////////////////////////////////////
  // Layer 1 - Hazelcast Jet Run Job //
  /////////////////////////////////////

  /**
   * Needs a JetInstance and Pipeline defined in this factors. Adds the pipeline to the existing
   * JetInstance as a job.
   *
   * @param jobName The name of the job.
   * @throws Exception If either no JetInstance or Pipeline is set, a job cannot be startet.
   */
  public void runUc4Job(final String jobName) throws IllegalStateException { // NOPMD

    // Check if a Jet Instance for UC4 is set.
    if (this.uc4JetInstance == null) {
      throw new IllegalStateException("Jet Instance is not set! "
          + "Cannot start a hazelcast jet job for UC4.");
    }

    // Check if a Pipeline for UC3 is set.
    if (this.uc4JetPipeline == null) {
      throw new IllegalStateException(
          "Hazelcast Pipeline is not set! Cannot start a hazelcast jet job for UC4.");
    }

    // Adds the job name and joins a job to the JetInstance defined in this factory
    final JobConfig jobConfig = new JobConfig()
        .registerSerializer(ValueGroup.class, ValueGroupSerializer.class)
        .registerSerializer(SensorGroupKey.class, SensorGroupKeySerializer.class)
        .registerSerializer(ImmutableSensorRegistry.class,
            ImmutableSensorRegistryUc4Serializer.class)
        .setName(jobName);
    this.uc4JetInstance.newJobIfAbsent(this.uc4JetPipeline, jobConfig).join();
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
   * @return A Uc4HazelcastJetFactory containing a set JetInstance.
   */
  public Uc4HazelcastJetFactory buildUc4JetInstanceFromEnv(final Logger logger,
      final String bootstrapServerDefault,
      final String hzKubernetesServiceDnsKey) {
    this.uc4JetInstance = new JetInstanceBuilder()
        .setConfigFromEnv(logger, bootstrapServerDefault, hzKubernetesServiceDnsKey)
        .build();
    return this;
  }

  /**
   * Builds a Hazelcast Jet pipeline used for a JetInstance to run it as a job on. Needs the input
   * topic and kafka properties defined in this factory beforehand.
   *
   * @return A Uc4HazelcastJetFactory containg a set pipeline.
   * @throws Exception If the input topic or the kafka properties are not defined, the pipeline
   *         cannot be built.
   */
  public Uc4HazelcastJetFactory buildUc4Pipeline() throws IllegalStateException { // NOPMD

    final String defaultPipelineWarning = "Cannot build pipeline."; // NOPMD

    // Check if Properties for the Kafka Input are set.
    if (this.kafkaInputReadPropsForPipeline == null) {
      throw new IllegalStateException("Kafka Input Read Properties for pipeline not set! "
          + defaultPipelineWarning);
    }

    // Check if Properties for the Kafka Output are set.
    if (this.kafkaWritePropsForPipeline == null) {
      throw new IllegalStateException("Kafka Write Properties for pipeline not set! "
          + defaultPipelineWarning);
    }

    // Check if Properties for the Kafka Config Read are set.
    if (this.kafkaConfigPropsForPipeline == null) {
      throw new IllegalStateException("Kafka Config Read Properties for pipeline not set! "
          + defaultPipelineWarning);
    }

    // Check if Properties for the Kafka Feedback Read are set.
    if (this.kafkaFeedbackPropsForPipeline == null) {
      throw new IllegalStateException("Kafka Feedback Read Properties for pipeline not set! "
          + defaultPipelineWarning);
    }

    // Check if the Kafka input topic is set.
    if (this.kafkaInputTopic == null) {
      throw new IllegalStateException("Kafka input topic for pipeline not set! "
          + defaultPipelineWarning);
    }

    // Check if the Kafka output topic is set.
    if (this.kafkaOutputTopic == null) {
      throw new IllegalStateException("kafka output topic for pipeline not set! "
          + defaultPipelineWarning);
    }

    // Check if the Kafka config topic is set.
    if (this.kafkaConfigurationTopic == null) {
      throw new IllegalStateException("configuratin topic for pipeline not set! "
          + defaultPipelineWarning);
    }

    // Check if the Kafka feedback topic is set.
    if (this.kafkaFeedbackTopic == null) {
      throw new IllegalStateException("Feedback topic not set! "
          + defaultPipelineWarning);
    }

    // Check if window size for tumbling window is set.
    if (this.windowSize <= 0) {
      throw new IllegalStateException("window size for pipeline not set or not greater than 0! "
          + defaultPipelineWarning);
    }

    // Build Pipeline Using the pipelineBuilder
    final Uc4PipelineBuilder pipeBuilder = new Uc4PipelineBuilder();
    this.uc4JetPipeline =
        pipeBuilder.build(this.kafkaInputReadPropsForPipeline,
            this.kafkaConfigPropsForPipeline,
            this.kafkaFeedbackPropsForPipeline,
            this.kafkaWritePropsForPipeline,
            this.kafkaInputTopic, this.kafkaOutputTopic,
            this.kafkaConfigurationTopic,
            this.kafkaFeedbackTopic,
            this.windowSize);
    // Return Uc4HazelcastJetBuilder factory
    return this;
  }

  /////////////
  // Layer 3 //
  /////////////

  /**
   * Sets kafka read properties for pipeline used in this builder using environment variables.
   *
   * @param bootstrapServersDefault Default Bootstrap server in the case that no bootstrap server
   *        can be fetched from the environment.
   * @param schemaRegistryUrlDefault Default schema registry url in the case that no schema registry
   *        url can be fetched from the environment.
   * @return The Uc4HazelcastJetBuilder factory with set kafkaReadPropertiesForPipeline.
   */
  public Uc4HazelcastJetFactory setReadPropertiesFromEnv(// NOPMD
      final String bootstrapServersDefault,
      final String schemaRegistryUrlDefault) {
    // Use KafkaPropertiesBuilder to build a properties object used for kafka
    final Uc4KafkaPropertiesBuilder propsBuilder = new Uc4KafkaPropertiesBuilder();
    final Properties kafkaInputReadProps =
        propsBuilder.buildKafkaInputReadPropsFromEnv(bootstrapServersDefault,
            schemaRegistryUrlDefault);
    final Properties kafkaConfigReadProps =
        propsBuilder.buildKafkaConfigReadPropsFromEnv(bootstrapServersDefault,
            schemaRegistryUrlDefault);
    final Properties kafkaAggregationReadProps =
        propsBuilder.buildKafkaAggregationReadPropsFromEnv(bootstrapServersDefault,
            schemaRegistryUrlDefault);
    this.kafkaInputReadPropsForPipeline = kafkaInputReadProps;
    this.kafkaConfigPropsForPipeline = kafkaConfigReadProps;
    this.kafkaFeedbackPropsForPipeline = kafkaAggregationReadProps;
    return this;
  }

  /**
   * Sets kafka write properties for pipeline used in this builder using environment variables.
   *
   * @param bootstrapServersDefault Default Bootstrap server in the case that no bootstrap server
   *        can be fetched from the environment.
   * @return The Uc4HazelcastJetBuilder factory with set kafkaWritePropertiesForPipeline.
   */
  public Uc4HazelcastJetFactory setWritePropertiesFromEnv(// NOPMD
      final String bootstrapServersDefault, final String schemaRegistryUrlDefault) {
    // Use KafkaPropertiesBuilder to build a properties object used for kafka
    final Uc4KafkaPropertiesBuilder propsBuilder = new Uc4KafkaPropertiesBuilder();
    final Properties kafkaWriteProps =
        propsBuilder.buildKafkaWritePropsFromEnv(bootstrapServersDefault, schemaRegistryUrlDefault);
    this.kafkaWritePropsForPipeline = kafkaWriteProps;
    return this;
  }

  /**
   * Sets the kafka input topic for the pipeline used in this builder.
   *
   * @param inputTopic The kafka topic used as the pipeline input.
   * @return A Uc4HazelcastJetBuilder factory with a set kafkaInputTopic.
   */
  public Uc4HazelcastJetFactory setCustomKafkaInputTopic(// NOPMD
      final String inputTopic) {
    this.kafkaInputTopic = inputTopic;
    return this;
  }

  /**
   * Sets the kafka input output for the pipeline used in this builder.
   *
   * @param outputTopic The kafka topic used as the pipeline output.
   * @return A Uc4HazelcastJetBuilder factory with a set kafkaOutputTopic.
   */
  public Uc4HazelcastJetFactory setCustomKafkaOutputTopic(final String outputTopic) { // NOPMD
    this.kafkaOutputTopic = outputTopic;
    return this;
  }


  /**
   * Sets the kafka input topic for the pipeline used in this builder using environment variables.
   *
   * @param defaultInputTopic The default kafka input topic used if no topic is specified by the
   *        environment.
   * @return A Uc4HazelcastJetBuilder factory with a set kafkaInputTopic.
   */
  public Uc4HazelcastJetFactory setKafkaInputTopicFromEnv(// NOPMD
      final String defaultInputTopic) {
    this.kafkaInputTopic = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_INPUT_TOPIC),
        defaultInputTopic);
    return this;
  }

  /**
   * Sets the kafka output topic for the pipeline used in this builder using environment variables.
   *
   * @param defaultOutputTopic The default kafka output topic used if no topic is specified by the
   *        environment.
   * @return A Uc4HazelcastJetBuilder factory with a set kafkaOutputTopic.
   */
  public Uc4HazelcastJetFactory setKafkaOutputTopicFromEnv(// NOPMD
      final String defaultOutputTopic) {
    this.kafkaOutputTopic = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_OUTPUT_TOPIC),
        defaultOutputTopic);
    return this;
  }

  /**
   * Sets the window size for the pipeline used in this builder.
   *
   * @param windowSize the window size to be used for this pipeline.
   * @return A Uc4HazelcastJetFactory with a set windowSize.
   */
  public Uc4HazelcastJetFactory setCustomWindowSize(// NOPMD
      final int windowSize) {
    this.windowSize = windowSize;
    return this;
  }

  /**
   * Sets the window size for the pipeline used in this builder from the environment.
   *
   * @param defaultWindowSize the default window size to be used for this pipeline when none is set
   *        in the environment.
   * @return A Uc4HazelcastJetFactory with a set windowSize.
   */
  public Uc4HazelcastJetFactory setWindowSizeFromEnv(// NOPMD
      final String defaultWindowSize) {
    final String windowSize = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.WINDOW_SIZE_UC4),
        defaultWindowSize);
    final int windowSizeNumber = Integer.parseInt(windowSize);
    this.windowSize = windowSizeNumber;
    return this;
  }

  /**
   * Sets the configuration topic for the pipeline used in this builder.
   *
   * @param kafkaConfigurationTopic the configuration topic to be used for this pipeline.
   * @return A Uc4HazelcastJetFactory with a set configuration topic.
   */
  public Uc4HazelcastJetFactory setCustomKafkaConfigurationTopic(// NOPMD
      final String kafkaConfigurationTopic) {
    this.kafkaConfigurationTopic = kafkaConfigurationTopic;
    return this;
  }

  /**
   * Sets the configuration topic for the pipeline used in this builder from the environment.
   *
   * @param defaultKafkaConfigurationTopic the default configuration topic to be used for this
   *        pipeline when none is set in the environment.
   * @return A Uc4HazelcastJetFactory with a set kafkaConfigurationTopic.
   */
  public Uc4HazelcastJetFactory setKafkaConfigurationTopicFromEnv(// NOPMD
      final String defaultKafkaConfigurationTopic) {
    this.kafkaConfigurationTopic = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_CONFIGURATION_TOPIC),
        defaultKafkaConfigurationTopic);
    return this;
  }

  /**
   * Sets the Feedback topic for the pipeline used in this builder.
   *
   * @param kafkaFeedbackTopic the Feedback topic to be used for this pipeline.
   * @return A Uc4HazelcastJetFactory with a set Feedback topic.
   */
  public Uc4HazelcastJetFactory setCustomKafkaFeedbackTopic(// NOPMD
      final String kafkaFeedbackTopic) {
    this.kafkaFeedbackTopic = kafkaFeedbackTopic;
    return this;
  }

  /**
   * Sets the Feedback topic for the pipeline used in this builder from the environment.
   *
   * @param defaultKafkaFeedbackTopic the default Feedback topic to be used for this pipeline when
   *        none is set in the environment.
   * @return A Uc4HazelcastJetFactory with a set kafkaFeedbackTopic.
   */
  public Uc4HazelcastJetFactory setKafkaFeedbackTopicFromEnv(// NOPMD
      final String defaultKafkaFeedbackTopic) {
    this.kafkaFeedbackTopic = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_FEEDBACK_TOPIC),
        defaultKafkaFeedbackTopic);
    return this;
  }

}
