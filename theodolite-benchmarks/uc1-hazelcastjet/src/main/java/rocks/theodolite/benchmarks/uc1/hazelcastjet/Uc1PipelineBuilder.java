package rocks.theodolite.benchmarks.uc1.hazelcastjet;

import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sink;
import com.hazelcast.jet.pipeline.SinkBuilder;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;
import java.util.Map.Entry;
import java.util.Properties;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseAdapter;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseWriter;
import rocks.theodolite.benchmarks.uc1.commons.logger.LogWriterFactory;


/**
 * Builder to build a HazelcastJet Pipeline for UC1 which can be used for stream processing using
 * Hazelcast Jet.
 */
public class Uc1PipelineBuilder {

  private final DatabaseAdapter<String> databaseAdapter = LogWriterFactory.forJson();

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
    // Do not refactor this to just use the call
    // (There is a problem with static calls in functions in hazelcastjet)
    final DatabaseWriter<String> writer = this.databaseAdapter.getDatabaseWriter();
    final Sink<String> sink = SinkBuilder.sinkBuilder(
        "Sink into database", x -> writer)
        .<String>receiveFn(DatabaseWriter::write)
        .build();

    uc1TopologyProduct.writeTo(sink);

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
        .setName("Convert content")
        .map(Entry::getValue)
        .map(this.databaseAdapter.getRecordConverter()::convert);
  }
}
