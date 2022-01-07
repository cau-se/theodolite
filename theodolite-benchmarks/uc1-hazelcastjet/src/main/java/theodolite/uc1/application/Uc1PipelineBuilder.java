package theodolite.uc1.application;

import com.google.gson.Gson;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;
import java.util.Map.Entry;
import java.util.Properties;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Builder to build a HazelcastJet Pipeline for UC1 which can be used for stream processing using
 * Hazelcast Jet.
 */
public class Uc1PipelineBuilder {

  private static final Gson GSON = new Gson();

  /**
   * Builds a pipeline which can be used for stream processing using Hazelcast Jet.
   *
   * @param kafkaPropsForPipeline Properties object containing the necessary Kafka attributes.
   * @param kafkaInputTopic The name of the input topic used for the pipeline.
   * @return A Hazelcast Jet pipeline which processes data for Uc1.
   */
  public Pipeline build(final Properties kafkaPropsForPipeline, final String kafkaInputTopic) {

    // Define a new pipeline
    final Pipeline pipe = Pipeline.create();

    // Define the Kafka Source
    final StreamSource<Entry<String, ActivePowerRecord>> kafkaSource =
        KafkaSources.<String, ActivePowerRecord>kafka(kafkaPropsForPipeline, kafkaInputTopic);

    // Extend UC1 topology to the pipeline
    final StreamStage<String> uc1TopologyProduct = this.extendUc1Topology(pipe, kafkaSource);

    // Add Sink: Logger
    uc1TopologyProduct.writeTo(Sinks.logger());

    return pipe;
  }

  /**
   * Extends to a blank Hazelcast Jet Pipeline the UC1 topology defines by Theodolite.
   *
   * <p>
   * UC1 takes {@code Entry<String,ActivePowerRecord>} objects and turns them into JSON strings
   * using GSON.
   * </p>
   *
   * @param pipe The blank hazelcast jet pipeline to extend the logic to.
   * @param source A streaming source to fetch data from.
   * @return A {@code StreamStage<String>} with the above definition of the String. It can be used
   *         to be further modified or directly be written into a sink.
   */
  public StreamStage<String> extendUc1Topology(final Pipeline pipe,
      final StreamSource<Entry<String, ActivePowerRecord>> source) {
    // Build the pipeline topology
    return pipe.readFrom(source)
        .withNativeTimestamps(0)
        .setLocalParallelism(1)
        .setName("Log content")
        .map(record -> {
          return GSON.toJson(record);
        });
  }

}
