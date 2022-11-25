package rocks.theodolite.benchmarks.uc2.hazelcastjet;

import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.jet.pipeline.WindowDefinition;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import rocks.theodolite.benchmarks.commons.hazelcastjet.PipelineFactory;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;


/**
 * PipelineFactory for use case 2. Allows to build and extend a pipeline.
 */
public class Uc2PipelineFactory extends PipelineFactory {

  private final Duration downsampleInterval;

  /**
   * Factory for uc2 pipelines.
   *
   * @param kafkaReadPropsForPipeline Properties Object containing the necessary kafka reads
   *        attributes.
   * @param kafkaInputTopic The name of the input topic used for the pipeline.
   * @param kafkaWritePropsForPipeline Properties Object containing the necessary kafka write
   *        attributes.
   * @param kafkaOutputTopic The name of the output topic used for the pipeline.
   * @param downsampleIntervalInMs The window length of the tumbling window used in the aggregation
   *        of this pipeline.
   */
  protected Uc2PipelineFactory(final Properties kafkaReadPropsForPipeline,
      final String kafkaInputTopic,
      final Properties kafkaWritePropsForPipeline,
      final String kafkaOutputTopic,
      final Duration downsampleIntervalInMs) {
    super(kafkaReadPropsForPipeline, kafkaInputTopic,
        kafkaWritePropsForPipeline, kafkaOutputTopic);
    this.downsampleInterval = downsampleIntervalInMs;
  }

  /**
   * Builds a pipeline which can be used for stream processing using Hazelcast Jet.
   *
   * @return returns a Pipeline used which can be used in a Hazelcast Jet Instance to process data
   *         for UC2.
   */
  @Override
  public Pipeline buildPipeline() {

    // Define the Kafka Source
    final StreamSource<Map.Entry<String, ActivePowerRecord>> kafkaSource =
        KafkaSources.<String, ActivePowerRecord>kafka(this.kafkaReadPropsForPipeline,
            this.kafkaInputTopic);

    // Extend UC2 topology to the pipeline
    final StreamStage<Map.Entry<String, String>> uc2TopologyProduct =
        this.extendUc2Topology(kafkaSource);

    // Add Sink1: Logger
    uc2TopologyProduct.writeTo(Sinks.logger()); // TODO align implementations
    // Add Sink2: Write back to kafka for the final benchmark
    uc2TopologyProduct.writeTo(KafkaSinks.<String, String>kafka(
        this.kafkaWritePropsForPipeline, this.kafkaOutputTopic));

    return this.pipe;
  }

  /**
   * Extends to a blank Hazelcast Jet Pipeline the UC2 topology defined by theodolite.
   *
   * <p>
   * UC2 takes {@code ActivePowerRecord} objects, groups them by keys, windows them in a tumbling
   * window and aggregates them into {@code Stats} objects. The final map returns an
   * {@code Entry<String,String>} where the key is the key of the group and the String is the
   * {@code .toString()} representation of the {@code Stats} object.
   * </p>
   *
   * @param source A streaming source to fetch data from.
   * @return A {@code StreamStage<Map.Entry<String,String>>} with the above definition of the key
   *         and value of the Entry object. It can be used to be further modified or directly be
   *         written into a sink.
   */
  public StreamStage<Map.Entry<String, String>> extendUc2Topology(
      final StreamSource<Map.Entry<String, ActivePowerRecord>> source) {
    // Build the pipeline topology.
    return this.pipe.readFrom(source)
        .withNativeTimestamps(0)
        .setLocalParallelism(1)
        .groupingKey(record -> record.getValue().getIdentifier())
        .window(WindowDefinition.tumbling(this.downsampleInterval.toMillis()))
        .aggregate(StatsAggregatorFactory.create())
        .map(agg -> {
          final String theKey = agg.key();
          final String theValue = agg.getValue().toString();
          return Map.entry(theKey, theValue);
        });
  }

}
