package rocks.theodolite.benchmarks.uc3.hazelcastjet;

import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import java.util.Objects;
import java.util.Properties;
import org.slf4j.Logger;
import rocks.theodolite.benchmarks.commons.hazelcastjet.ConfigurationKeys;
import rocks.theodolite.benchmarks.commons.hazelcastjet.JetInstanceBuilder;
import rocks.theodolite.benchmarks.uc3.hazelcastjet.uc3specifics.HourOfDayKey;
import rocks.theodolite.benchmarks.uc3.hazelcastjet.uc3specifics.HourOfDayKeySerializer;

/**
 * A Hazelcast Jet factory which can build a Hazelcast Jet Instance and Pipeline for the UC3
 * benchmark and lets you start the Hazelcast Jet job. The JetInstance can be built directly as the
 * Hazelcast Config is managed internally. In order to build the Pipeline, you first have to build
 * the Read and Write Properties, set the input and output topic, and set the window size in seconds
 * and the hopping size in seconds. This can be done using internal functions of this factory.
 * Outside data only refers to custom values or default values in case data of the environment
 * cannot the fetched.
 */
public class Uc3HazelcastJetFactory { // NOPMD

  // Information per History Service
  private Properties kafkaReadPropsForPipeline;
  private Properties kafkaWritePropsForPipeline;
  private String kafkaInputTopic;
  private String kafkaOutputTopic;
  private JetInstance uc3JetInstance;
  private Pipeline uc3JetPipeline;
  // UC3 specific
  private int windowSizeInSeconds;
  private int hoppingSizeInSeconds;

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
  public void runUc3Job(final String jobName) throws IllegalStateException { // NOPMD

    // Check if a Jet Instance for UC3 is set.
    if (this.uc3JetInstance == null) {
      throw new IllegalStateException("Jet Instance is not set! "
          + "Cannot start a hazelcast jet job for UC3.");
    }

    // Check if a Pipeline for UC3 is set.
    if (this.uc3JetPipeline == null) {
      throw new IllegalStateException(
          "Hazelcast Pipeline is not set! Cannot start a hazelcast jet job for UC3.");
    }

    // Adds the job name and joins a job to the JetInstance defined in this factory
    final JobConfig jobConfig = new JobConfig()
        .registerSerializer(HourOfDayKey.class, HourOfDayKeySerializer.class)
        .setName(jobName);
    this.uc3JetInstance.newJobIfAbsent(this.uc3JetPipeline, jobConfig).join();
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
   * @return A Uc3HazelcastJetFactory containing a set JetInstance.
   */
  public Uc3HazelcastJetFactory buildUc3JetInstanceFromEnv(final Logger logger,
      final String bootstrapServerDefault,
      final String hzKubernetesServiceDnsKey) {
    this.uc3JetInstance = new JetInstanceBuilder()
        .setConfigFromEnv(logger, bootstrapServerDefault, hzKubernetesServiceDnsKey)
        .build();
    return this;
  }

  /**
   * Builds a Hazelcast Jet pipeline used for a JetInstance to run it as a job on. Needs the input
   * topic and kafka properties defined in this factory beforehand.
   *
   * @return A Uc3HazelcastJetFactory containg a set pipeline.
   * @throws Exception If the input topic or the kafka properties are not defined, the pipeline
   *         cannot be built.
   */
  public Uc3HazelcastJetFactory buildUc3Pipeline() throws IllegalStateException { // NOPMD

    final String defaultPipelineWarning = "Cannot build pipeline."; // NOPMD

    // Check if Properties for the Kafka Input are set.
    if (this.kafkaReadPropsForPipeline == null) {
      throw new IllegalStateException("Kafka Read Properties for pipeline not set! "
          + defaultPipelineWarning);
    }

    // Check if Properties for the Kafka Output are set.
    if (this.kafkaWritePropsForPipeline == null) {
      throw new IllegalStateException("Kafka Write Properties for pipeline not set! "
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

    // Check if the window size for the "sliding" window is set.
    if (this.windowSizeInSeconds <= 0) {
      throw new IllegalStateException(
          "window size in seconds for pipeline not set or not greater than 0! "
              + defaultPipelineWarning);
    }

    // Check if the hopping distance for the "sliding" window is set.
    if (this.hoppingSizeInSeconds <= 0) {
      throw new IllegalStateException(
          "hopping size in seconds for pipeline not set or not greater than 0! "
              + defaultPipelineWarning);
    }

    // Build Pipeline Using the pipelineBuilder
    final Uc3PipelineBuilder pipeBuilder = new Uc3PipelineBuilder();
    this.uc3JetPipeline =
        pipeBuilder.build(this.kafkaReadPropsForPipeline,
            this.kafkaWritePropsForPipeline,
            this.kafkaInputTopic, this.kafkaOutputTopic, this.hoppingSizeInSeconds,
            this.windowSizeInSeconds);
    // Return Uc3HazelcastJetBuilder factory
    return this;
  }

  /////////////
  // Layer 3 //
  /////////////

  /**
   * Sets kafka read properties for pipeline used in this builder.
   *
   * @param kafkaReadProperties A propeties object containing necessary values used for the hazelcst
   *        jet kafka connection to read data.
   * @return The Uc3HazelcastJetBuilder factory with set kafkaReadPropsForPipeline.
   */
  public Uc3HazelcastJetFactory setCustomReadProperties(// NOPMD
      final Properties kafkaReadProperties) {
    this.kafkaReadPropsForPipeline = kafkaReadProperties;
    return this;
  }

  /**
   * Sets kafka write properties for pipeline used in this builder.
   *
   * @param kafkaWriteProperties A propeties object containing necessary values used for the
   *        hazelcst jet kafka connection to write data.
   * @return The Uc3HazelcastJetBuilder factory with set kafkaWritePropsForPipeline.
   */
  public Uc3HazelcastJetFactory setCustomWriteProperties(// NOPMD
      final Properties kafkaWriteProperties) {
    this.kafkaWritePropsForPipeline = kafkaWriteProperties;
    return this;
  }

  /**
   * Sets kafka read properties for pipeline used in this builder using environment variables.
   *
   * @param bootstrapServersDefault Default Bootstrap server in the case that no bootstrap server
   *        can be fetched from the environment.
   * @param schemaRegistryUrlDefault Default schema registry url in the case that no schema registry
   *        url can be fetched from the environment.
   * @return The Uc3HazelcastJetBuilder factory with set kafkaReadPropertiesForPipeline.
   */
  public Uc3HazelcastJetFactory setReadPropertiesFromEnv(// NOPMD
      final String bootstrapServersDefault,
      final String schemaRegistryUrlDefault) {
    // Use KafkaPropertiesBuilder to build a properties object used for kafka
    final Uc3KafkaPropertiesBuilder propsBuilder = new Uc3KafkaPropertiesBuilder();
    final Properties kafkaReadProps =
        propsBuilder.buildKafkaReadPropsFromEnv(bootstrapServersDefault,
            schemaRegistryUrlDefault);
    this.kafkaReadPropsForPipeline = kafkaReadProps;
    return this;
  }

  /**
   * Sets kafka write properties for pipeline used in this builder using environment variables.
   *
   * @param bootstrapServersDefault Default Bootstrap server in the case that no bootstrap server
   *        can be fetched from the environment.
   * @return The Uc3HazelcastJetBuilder factory with set kafkaWritePropertiesForPipeline.
   */
  public Uc3HazelcastJetFactory setWritePropertiesFromEnv(// NOPMD
      final String bootstrapServersDefault) {
    // Use KafkaPropertiesBuilder to build a properties object used for kafka
    final Uc3KafkaPropertiesBuilder propsBuilder = new Uc3KafkaPropertiesBuilder();
    final Properties kafkaWriteProps =
        propsBuilder.buildKafkaWritePropsFromEnv(bootstrapServersDefault);
    this.kafkaWritePropsForPipeline = kafkaWriteProps;
    return this;
  }

  /**
   * Sets the kafka input topic for the pipeline used in this builder.
   *
   * @param inputTopic The kafka topic used as the pipeline input.
   * @return A Uc3HazelcastJetBuilder factory with a set kafkaInputTopic.
   */
  public Uc3HazelcastJetFactory setCustomKafkaInputTopic(// NOPMD
      final String inputTopic) {
    this.kafkaInputTopic = inputTopic;
    return this;
  }

  /**
   * Sets the kafka input output for the pipeline used in this builder.
   *
   * @param outputTopic The kafka topic used as the pipeline output.
   * @return A Uc3HazelcastJetBuilder factory with a set kafkaOutputTopic.
   */
  public Uc3HazelcastJetFactory setCustomKafkaOutputTopic(final String outputTopic) { // NOPMD
    this.kafkaOutputTopic = outputTopic;
    return this;
  }


  /**
   * Sets the kafka input topic for the pipeline used in this builder using environment variables.
   *
   * @param defaultInputTopic The default kafka input topic used if no topic is specified by the
   *        environment.
   * @return A Uc3HazelcastJetBuilder factory with a set kafkaInputTopic.
   */
  public Uc3HazelcastJetFactory setKafkaInputTopicFromEnv(// NOPMD
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
   * @return A Uc3HazelcastJetBuilder factory with a set kafkaOutputTopic.
   */
  public Uc3HazelcastJetFactory setKafkaOutputTopicFromEnv(// NOPMD
      final String defaultOutputTopic) {
    this.kafkaOutputTopic = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_OUTPUT_TOPIC),
        defaultOutputTopic);
    return this;
  }

  /**
   * Sets the window size in seconds for the pipeline used in this builder.
   *
   * @param windowSizeInSeconds the windowSizeInSeconds to be used for this pipeline.
   * @return A Uc3HazelcastJetFactory with a set windowSizeInSeconds.
   */
  public Uc3HazelcastJetFactory setCustomWindowSizeInSeconds(// NOPMD
      final int windowSizeInSeconds) {
    this.windowSizeInSeconds = windowSizeInSeconds;
    return this;
  }

  /**
   * Sets the window size in seconds for the pipeline used in this builder from the environment.
   *
   * @param defaultWindowSizeInSeconds the default window size in seconds to be used for this
   *        pipeline when none is set in the environment.
   * @return A Uc3HazelcastJetFactory with a set windowSizeInSeconds.
   */
  public Uc3HazelcastJetFactory setWindowSizeInSecondsFromEnv(// NOPMD
      final String defaultWindowSizeInSeconds) {
    final String windowSizeInSeconds = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.WINDOW_SIZE_IN_SECONDS),
        defaultWindowSizeInSeconds);
    final int windowSizeInSecondsNumber = Integer.parseInt(windowSizeInSeconds);
    this.windowSizeInSeconds = windowSizeInSecondsNumber;
    return this;
  }

  /**
   * Sets the hopping size in seconds for the pipeline used in this builder.
   *
   * @param hoppingSizeInSeconds the hoppingSizeInSeconds to be used for this pipeline.
   * @return A Uc3HazelcastJetFactory with a set hoppingSizeInSeconds.
   */
  public Uc3HazelcastJetFactory setCustomHoppingSizeInSeconds(// NOPMD
      final int hoppingSizeInSeconds) {
    this.hoppingSizeInSeconds = hoppingSizeInSeconds;
    return this;
  }

  /**
   * Sets the hopping size in seconds for the pipeline used in this builder from the environment.
   *
   * @param defaultHoppingSizeInSeconds the default hopping size in seconds to be used for this
   *        pipeline when none is set in the environment.
   * @return A Uc3HazelcastJetFactory with a set hoppingSizeInSeconds.
   */
  public Uc3HazelcastJetFactory setHoppingSizeInSecondsFromEnv(// NOPMD
      final String defaultHoppingSizeInSeconds) {
    final String hoppingSizeInSeconds = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.HOPPING_SIZE_IN_SECONDS),
        defaultHoppingSizeInSeconds);
    final int hoppingSizeInSecondsNumber = Integer.parseInt(hoppingSizeInSeconds);
    this.hoppingSizeInSeconds = hoppingSizeInSecondsNumber;
    return this;
  }
}
