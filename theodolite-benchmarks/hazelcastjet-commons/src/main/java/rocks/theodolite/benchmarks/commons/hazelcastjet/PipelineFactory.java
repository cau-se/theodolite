package rocks.theodolite.benchmarks.commons.hazelcastjet;

import com.hazelcast.jet.pipeline.Pipeline;

import java.util.Properties;

/**
 * Abstract class to handle the common logic for all pipelines.
 * Implement {@link #buildPipeline()} method to implement the custom logic of the use case.
 * Caution implement this with the help of an extendPipeline() method in order to
 * be testable without further infrastructure.
 * A template for this is construct the sources in {@link #buildPipeline()} and give them
 * as parameters to extendTopology(...).
 * Further implement the pipeline logic in the extendPipeline() method and return the last stage.
 * Use the returned stage afterwards in the {@link #buildPipeline()} to write the results.
 */
public abstract class PipelineFactory {

  protected final Pipeline pipe;

  protected Properties kafkaReadPropsForPipeline;
  protected Properties kafkaWritePropsForPipeline;

  protected String kafkaInputTopic;
  protected String kafkaOutputTopic;


  public PipelineFactory() {
    this.pipe = Pipeline.create();
  }

  /**
   * Constructs a pipeline factory with read properties and input topic.
   * Directly used for Uc1.
   */
  public PipelineFactory(final Properties kafkaReadPropsForPipeline,
                         final String kafkaInputTopic) {
    this();
    this.kafkaReadPropsForPipeline = kafkaReadPropsForPipeline;
    this.kafkaInputTopic = kafkaInputTopic;
  }

  /**
   * Constructs a pipeline factory with read/write properties and input/output topic.
   */
  public PipelineFactory(final Properties kafkaReadPropsForPipeline,
                         final String kafkaInputTopic,
                         final Properties kafkaWritePropsForPipeline,
                         final String kafkaOutputTopic) {
    this(kafkaReadPropsForPipeline, kafkaInputTopic);
    this.kafkaWritePropsForPipeline = kafkaWritePropsForPipeline;
    this.kafkaOutputTopic = kafkaOutputTopic;
  }

  /**
   * Implement to construct the use case logic.
   * @return pipeline that holds the use case logic.
   */
  public abstract Pipeline buildPipeline();

}
