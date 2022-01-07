package theodolite.uc2.application;

import com.google.common.math.Stats;
import com.google.common.math.StatsAccumulator;
import com.hazelcast.jet.aggregate.AggregateOperation;
import com.hazelcast.jet.aggregate.AggregateOperation1;
import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.jet.pipeline.WindowDefinition;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import theodolite.uc2.application.uc2specifics.StatsAccumulatorSupplier;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Builder to build a HazelcastJet Pipeline for UC2 which can be used for stream processing using
 * Hazelcast Jet.
 */
public class Uc2PipelineBuilder {

  /**
   * Builds a pipeline which can be used for stream processing using Hazelcast Jet.
   *
   * @param kafkaReadPropsForPipeline Properties Object containing the necessary kafka reads
   *        attributes.
   * @param kafkaWritePropsForPipeline Properties Object containing the necessary kafka write
   *        attributes.
   * @param kafkaInputTopic The name of the input topic used for the pipeline.
   * @param kafkaOutputTopic The name of the output topic used for the pipeline.
   * @param downsampleIntervalInMs The window length of the tumbling window used in the aggregation
   *        of this pipeline.
   * @return returns a Pipeline used which can be used in a Hazelcast Jet Instance to process data
   *         for UC2.
   */
  public Pipeline build(final Properties kafkaReadPropsForPipeline,
      final Properties kafkaWritePropsForPipeline, final String kafkaInputTopic,
      final String kafkaOutputTopic,
      final int downsampleIntervalInMs) {

    // Define a new pipeline
    final Pipeline pipe = Pipeline.create();

    // Define the Kafka Source
    final StreamSource<Entry<String, ActivePowerRecord>> kafkaSource =
        KafkaSources.<String, ActivePowerRecord>kafka(kafkaReadPropsForPipeline, kafkaInputTopic);

    // Extend UC2 topology to the pipeline
    final StreamStage<Map.Entry<String, String>> uc2TopologyProduct =
        this.extendUc2Topology(pipe, kafkaSource, downsampleIntervalInMs);

    // Add Sink1: Logger
    uc2TopologyProduct.writeTo(Sinks.logger());
    // Add Sink2: Write back to kafka for the final benchmark
    uc2TopologyProduct.writeTo(KafkaSinks.<String, String>kafka(
        kafkaWritePropsForPipeline, kafkaOutputTopic));

    return pipe;
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
   * @param pipe The blank hazelcast jet pipeline to extend the logic to.
   * @param source A streaming source to fetch data from.
   * @param downsampleIntervalInMs The size of the tumbling window.
   * @return A {@code StreamStage<Map.Entry<String,String>>} with the above definition of the key
   *         and value of the Entry object. It can be used to be further modified or directly be
   *         written into a sink.
   */
  public StreamStage<Map.Entry<String, String>> extendUc2Topology(final Pipeline pipe,
      final StreamSource<Entry<String, ActivePowerRecord>> source,
      final int downsampleIntervalInMs) {
    // Build the pipeline topology.
    return pipe.readFrom(source)
        .withNativeTimestamps(0)
        .setLocalParallelism(1)
        .groupingKey(record -> record.getValue().getIdentifier())
        .window(WindowDefinition.tumbling(downsampleIntervalInMs))
        .aggregate(this.uc2AggregateOperation())
        .map(agg -> {
          final String theKey = agg.key();
          final String theValue = agg.getValue().toString();
          return Map.entry(theKey, theValue);
        });
  }

  /**
   * Defines an AggregateOperation1 for Hazelcast Jet which is used in the Pipeline of the Hazelcast
   * Jet implementation of UC2.
   *
   * <p>
   * Takes a windowed and keyed {@code Entry<String,ActivePowerRecord>} elements and returns a
   * {@Stats} object.
   * </p>
   *
   * @return An AggregateOperation used by Hazelcast Jet in a streaming stage which aggregates
   *         ActivePowerRecord Objects into Stats Objects.
   */
  @SuppressWarnings("unchecked")
  public AggregateOperation1<Object, StatsAccumulator, Stats> uc2AggregateOperation() {

    // Aggregate Operation to Create a Stats Object from Entry<String,ActivePowerRecord> items using
    // the Statsaccumulator.
    return AggregateOperation
        // Creates the accumulator
        .withCreate(new StatsAccumulatorSupplier())
        // Defines the accumulation
        .andAccumulate((accumulator, item) -> {
          final Entry<String, ActivePowerRecord> castedEntry =
              (Entry<String, ActivePowerRecord>) item;
          accumulator.add(castedEntry.getValue().getValueInW());
        })
        // Defines the combination of spread out instances
        .andCombine((left, right) -> {
          final Stats rightStats = right.snapshot();
          left.addAll(rightStats);

        })
        // Finishes the aggregation
        .andExportFinish((accumulator) -> {
          return accumulator.snapshot();
        });
  }

}
