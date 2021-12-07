package theodolite.uc2.application;

import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import java.util.Objects;
import java.util.Properties;
import org.slf4j.Logger;
import theodolite.commons.hazelcastjet.ConfigurationKeys;
import theodolite.commons.hazelcastjet.JetInstanceBuilder;

/**
 * A Hazelcast Jet factory which can build a Hazelcast Jet Instance and Pipeline for the UC2
 * benchmark and lets you start the Hazelcast Jet job. The JetInstance can be built directly as the
 * Hazelcast Config is managed internally. In order to build the Pipeline, you first have to build
 * the Read and Write Properties, set the input and output topic, and set the downsample interval
 * which can be done using internal functions of this factory. Outside data only refers to custom
 * values or default values in case data of the environment cannot the fetched.
 */
public class Uc2HazelcastJetFactory {

  // Information per History Service
  private Properties kafkaReadPropsForPipeline;
  private Properties kafkaWritePropsForPipeline;
  private String kafkaInputTopic;
  private String kafkaOutputTopic;
  private JetInstance uc2JetInstance;
  private Pipeline uc2JetPipeline;
  // UC2 specific
  private int downsampleInterval;

  // Checkflags
  private boolean readPropertiesSet;
  private boolean writePropertiesSet;
  private boolean inputTopicSet;
  private boolean outputTopicSet;
  private boolean pipelineSet;
  private boolean jetInstanceSet;
  private boolean downsampleIntervalSet;

  /**
   * Create a new Hazelcast Jet Factory for UC2.
   */
  public Uc2HazelcastJetFactory() {
    this.readPropertiesSet = false;
    this.writePropertiesSet = false;
    this.inputTopicSet = false;
    this.outputTopicSet = false;
    this.pipelineSet = false;
    this.jetInstanceSet = false;
    this.downsampleIntervalSet = false;
  }

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
  public void runUc2Job(final String jobName) throws Exception { // NOPMD
    if (this.jetInstanceSet) {
      if (this.pipelineSet) {

        // Adds the job name and joins a job to the JetInstance defined in this factory
        final JobConfig jobConfig = new JobConfig();
        jobConfig.setName(jobName);
        this.uc2JetInstance.newJobIfAbsent(this.uc2JetPipeline, jobConfig).join();

      } else {
        throw new Exception(// NOPMD
            "Hazelcast Pipeline is not set! Cannot start a hazelcast jet job for UC2.");
      }
    } else {
      throw new Exception("Jet Instance is not set! " // NOPMD
          + "Cannot start a hazelcast jet job for UC2.");
    }
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
   * @return A Uc2HazelcastJetFactory containing a set JetInstance.
   */
  public Uc2HazelcastJetFactory buildUc2JetInstanceFromEnv(final Logger logger,
      final String bootstrapServerDefault,
      final String hzKubernetesServiceDnsKey) {
    this.uc2JetInstance = new JetInstanceBuilder()
        .setConfigFromEnv(logger, bootstrapServerDefault, hzKubernetesServiceDnsKey)
        .build();
    this.jetInstanceSet = true;
    return this;
  }

  /**
   * Builds a Hazelcast Jet pipeline used for a JetInstance to run it as a job on. Needs the input
   * topic and kafka properties defined in this factory beforehand.
   *
   * @return A Uc2HazelcastJetFactory containg a set pipeline.
   * @throws Exception If the input topic or the kafka properties are not defined, the pipeline
   *         cannot be built.
   */
  public Uc2HazelcastJetFactory buildUc2Pipeline() throws Exception { // NOPMD
    // Check for set properties and set input topic
    if (this.readPropertiesSet) {
      if (this.writePropertiesSet) {
        if (this.inputTopicSet) {
          if (this.outputTopicSet) {
            if (this.downsampleIntervalSet) {
              // Build Pipeline Using the pipelineBuilder
              final Uc2PipelineBuilder pipeBuilder = new Uc2PipelineBuilder();
              this.uc2JetPipeline =
                  pipeBuilder.build(this.kafkaReadPropsForPipeline, this.kafkaWritePropsForPipeline,
                      this.kafkaInputTopic, this.kafkaOutputTopic, this.downsampleInterval);
              this.pipelineSet = true;
              // Return Uc2HazelcastJetBuilder factory
              return this;
            } else {
              throw new Exception("downsample interval for pipeline not set! " // NOPMD
                  + "Cannot build pipeline."); // NOCS // NOPMD
            }
          } else {
            throw new Exception("kafka output topic for pipeline not set! " // NOPMD
                + "Cannot build pipeline."); // NOCS // NOPMD
          }
        } else {
          throw new Exception("Kafka input topic for pipeline not set! " // NOPMD
              + "Cannot build pipeline."); // NOCS // NOPMD
        }
      } else {
        throw new Exception("Kafka Write Properties for pipeline not set! " // NOPMD
            + "Cannot build pipeline."); // NOCS // NOPMD
      }
    } else {
      throw new Exception("Kafka Read Properties for pipeline not set! " // NOPMD
          + "Cannot build pipeline."); // NOCS // NOPMD
    }
  }

  /////////////
  // Layer 3 //
  /////////////

  /**
   * Sets kafka read properties for pipeline used in this builder.
   *
   * @param kafkaReadProperties A propeties object containing necessary values used for the hazelcst
   *        jet kafka connection to read data.
   * @return The Uc2HazelcastJetBuilder factory with set kafkaReadPropsForPipeline.
   */
  public Uc2HazelcastJetFactory setCustomReadProperties(// NOPMD
      final Properties kafkaReadProperties) {
    this.kafkaReadPropsForPipeline = kafkaReadProperties;
    this.readPropertiesSet = true;
    return this;
  }

  /**
   * Sets kafka write properties for pipeline used in this builder.
   *
   * @param kafkaWriteProperties A propeties object containing necessary values used for the
   *        hazelcst jet kafka connection to write data.
   * @return The Uc2HazelcastJetBuilder factory with set kafkaWritePropsForPipeline.
   */
  public Uc2HazelcastJetFactory setCustomWriteProperties(// NOPMD
      final Properties kafkaWriteProperties) {
    this.kafkaWritePropsForPipeline = kafkaWriteProperties;
    this.writePropertiesSet = true;
    return this;
  }

  /**
   * Sets kafka read properties for pipeline used in this builder using environment variables.
   *
   * @param bootstrapServersDefault Default Bootstrap server in the case that no bootstrap server
   *        can be fetched from the environment.
   * @param schemaRegistryUrlDefault Default schema registry url in the case that no schema registry
   *        url can be fetched from the environment.
   * @return The Uc2HazelcastJetBuilder factory with set kafkaReadPropertiesForPipeline.
   */
  public Uc2HazelcastJetFactory setReadPropertiesFromEnv(// NOPMD
      final String bootstrapServersDefault,
      final String schemaRegistryUrlDefault) {
    // Use KafkaPropertiesBuilder to build a properties object used for kafka
    final Uc2KafkaPropertiesBuilder propsBuilder = new Uc2KafkaPropertiesBuilder();
    final Properties kafkaReadProps =
        propsBuilder.buildKafkaReadPropsFromEnv(bootstrapServersDefault,
            schemaRegistryUrlDefault);
    this.kafkaReadPropsForPipeline = kafkaReadProps;
    this.readPropertiesSet = true;
    return this;
  }

  /**
   * Sets kafka write properties for pipeline used in this builder using environment variables.
   *
   * @param bootstrapServersDefault Default Bootstrap server in the case that no bootstrap server
   *        can be fetched from the environment.
   * @return The Uc2HazelcastJetBuilder factory with set kafkaWritePropertiesForPipeline.
   */
  public Uc2HazelcastJetFactory setWritePropertiesFromEnv(// NOPMD
      final String bootstrapServersDefault) {
    // Use KafkaPropertiesBuilder to build a properties object used for kafka
    final Uc2KafkaPropertiesBuilder propsBuilder = new Uc2KafkaPropertiesBuilder();
    final Properties kafkaWriteProps =
        propsBuilder.buildKafkaWritePropsFromEnv(bootstrapServersDefault);
    this.kafkaWritePropsForPipeline = kafkaWriteProps;
    this.writePropertiesSet = true;
    return this;
  }

  /**
   * Sets the kafka input topic for the pipeline used in this builder.
   *
   * @param inputTopic The kafka topic used as the pipeline input.
   * @return A Uc2HazelcastJetBuilder factory with a set kafkaInputTopic.
   */
  public Uc2HazelcastJetFactory setCustomKafkaInputTopic(// NOPMD
      final String inputTopic) {
    this.kafkaInputTopic = inputTopic;
    this.inputTopicSet = true;
    return this;
  }

  /**
   * Sets the kafka input output for the pipeline used in this builder.
   *
   * @param outputTopic The kafka topic used as the pipeline output.
   * @return A Uc2HazelcastJetBuilder factory with a set kafkaOutputTopic.
   */
  public Uc2HazelcastJetFactory setCustomKafkaOutputTopic(final String outputTopic) { // NOPMD
    this.kafkaOutputTopic = outputTopic;
    this.outputTopicSet = true;
    return this;
  }


  /**
   * Sets the kafka input topic for the pipeline used in this builder using environment variables.
   *
   * @param defaultInputTopic The default kafka input topic used if no topic is specified by the
   *        environment.
   * @return A Uc2HazelcastJetBuilder factory with a set kafkaInputTopic.
   */
  public Uc2HazelcastJetFactory setKafkaInputTopicFromEnv(// NOPMD
      final String defaultInputTopic) {
    this.kafkaInputTopic = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_INPUT_TOPIC),
        defaultInputTopic);
    this.inputTopicSet = true;
    return this;
  }

  /**
   * Sets the kafka output topic for the pipeline used in this builder using environment variables.
   *
   * @param defaultOutputTopic The default kafka output topic used if no topic is specified by the
   *        environment.
   * @return A Uc2HazelcastJetBuilder factory with a set kafkaOutputTopic.
   */
  public Uc2HazelcastJetFactory setKafkaOutputTopicFromEnv(// NOPMD
      final String defaultOutputTopic) {
    this.kafkaOutputTopic = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_OUTPUT_TOPIC),
        defaultOutputTopic);
    this.outputTopicSet = true;
    return this;
  }

  /**
   * Sets the downsample interval for the pipeline used in this builder.
   * 
   * @param downsampleInterval the downsample interval to be used for this pipeline.
   * @return A Uc2HazelcastJetFactory with a set downsampleInterval.
   */
  public Uc2HazelcastJetFactory setCustomDownsampleInterval(// NOPMD
      final int downsampleInterval) {
    this.downsampleInterval = downsampleInterval;
    this.downsampleIntervalSet = true;
    return this;
  }

  /**
   * Sets the downsample interval for the pipeline used in this builder from the environment.
   * 
   * @param defaultDownsampleInterval the default downsample interval to be used for this pipeline
   *        when none is set in the environment.
   * @return A Uc2HazelcastJetFactory with a set downsampleInterval.
   */
  public Uc2HazelcastJetFactory setDownsampleIntervalFromEnv(// NOPMD
      final String defaultDownsampleInterval) {
    final String downsampleInterval = (String) Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.DOWNSAMPLE_INTERVAL),
        defaultDownsampleInterval);
    final int downsampleIntervalNumber = Integer.parseInt(downsampleInterval);
    this.downsampleInterval = downsampleIntervalNumber;
    this.downsampleIntervalSet = true;
    return this;
  }

}
