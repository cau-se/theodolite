package theodolite.uc2.application;

import com.google.common.math.Stats;
import com.google.common.math.StatsAccumulator;
import com.hazelcast.jet.aggregate.AggregateOperation;
import com.hazelcast.jet.aggregate.AggregateOperation1;
import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
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
   * @param downsampleInterval The window length of the tumbling window used in the aggregation of
   *        this pipeline.
   * @return returns a Pipeline used which can be used in a Hazelcast Jet Instance to process data
   *         for UC2.
   */
  public Pipeline build(final Properties kafkaReadPropsForPipeline,
      final Properties kafkaWritePropsForPipeline, final String kafkaInputTopic,
      final String kafkaOutputTopic,
      final int downsampleInterval) {

    // Aggregate Operation to Create a Stats Object from Entry<String,ActivePowerRecord> items using
    // the Statsaccumulator.
    final AggregateOperation1<Object, StatsAccumulator, Stats> aggrOp = AggregateOperation
        .withCreate(new StatsAccumulatorSupplier())
        .andAccumulate((accumulator, item) -> {

          Entry<String, ActivePowerRecord> castedEntry = (Entry<String, ActivePowerRecord>) item;
          accumulator.add(castedEntry.getValue().getValueInW());

        })
        .andCombine((left, right) -> {

          Stats rightStats = right.snapshot();
          left.addAll(rightStats);

        })
        .andExportFinish((accumulator) -> {
          return accumulator.snapshot();
        });

    final Pipeline pipe = Pipeline.create();
    final StreamStage<Map.Entry<String, String>> mapProduct =
        pipe.readFrom(KafkaSources.<String, ActivePowerRecord>kafka(
            kafkaReadPropsForPipeline, kafkaInputTopic))
            .withNativeTimestamps(0)
            .setLocalParallelism(1)
            .groupingKey(record -> record.getValue().getIdentifier())
            .window(WindowDefinition.tumbling(downsampleInterval))
            .aggregate(aggrOp)
            .map(agg -> {
              String theKey = agg.key();
              String theValue = agg.getValue().toString();
              return Map.entry(theKey, theValue);
            });
    // Add Sink1: Logger
    mapProduct.writeTo(Sinks.logger());
    // Add Sink2: Write back to kafka for the final benchmark
    mapProduct.writeTo(KafkaSinks.<String, String>kafka(
        kafkaWritePropsForPipeline, kafkaOutputTopic));

    return pipe;
  }

}
