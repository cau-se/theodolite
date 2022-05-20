package rocks.theodolite.benchmarks.uc1.hazelcastjet;

import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sink;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;
import rocks.theodolite.benchmarks.commons.hazelcastjet.PipelineFactory;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseAdapter;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseWriter;
import rocks.theodolite.benchmarks.uc1.commons.logger.LogWriterFactory;
import titan.ccp.model.records.ActivePowerRecord;
import java.util.Map;
import java.util.Properties;
import static com.hazelcast.jet.pipeline.SinkBuilder.sinkBuilder;

public class Uc1PipelineFactory extends PipelineFactory {

  private final DatabaseAdapter<String> databaseAdapter = LogWriterFactory.forJson();

  private final Properties kafkaPropsForPipeline;
  private final String kafkaInputTopic;

  /**
   * Creates a new Uc1PipelineFactory.
   * @param kafkaPropsForPipeline Properties object containing the necessary Kafka attributes.
   * @param kafkaInputTopic The name of the input topic used for the pipeline.
   */
  public Uc1PipelineFactory(final Properties kafkaPropsForPipeline, final String kafkaInputTopic) {
    super();
    this.kafkaPropsForPipeline = kafkaPropsForPipeline;
    this.kafkaInputTopic = kafkaInputTopic;
  }

  /**
   * Builds a pipeline which can be used for stream processing using Hazelcast Jet.
   *
   * @return A Hazelcast Jet pipeline which processes data for Uc1.
   */
  public Pipeline buildPipeline() {

    // Define a new pipeline
    final Pipeline pipe = Pipeline.create();

    // Define the Kafka Source
    final StreamSource<Map.Entry<String, ActivePowerRecord>> kafkaSource =
        KafkaSources.<String, ActivePowerRecord>kafka(kafkaPropsForPipeline, kafkaInputTopic);

    // Extend UC1 topology to the pipeline
    final StreamStage<String> uc1TopologyProduct = this.extendUc1Topology(pipe, kafkaSource);

    // Add Sink: Logger
    // Do not refactor this to just use the call
    // (There is a problem with static calls in functions in hazelcastjet)
    final DatabaseWriter<String> writer = this.databaseAdapter.getDatabaseWriter();
    final Sink<String> sink = sinkBuilder(
        "Sink into database", x -> writer)
        .<String>receiveFn(DatabaseWriter::write)
        .build();

    uc1TopologyProduct.writeTo(sink);

    return pipe;
  }

  @Override
  public StreamStage extendTopology() {
    return null;
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
                                               final StreamSource<Map.Entry<String, ActivePowerRecord>> source) {

    // Build the pipeline topology
    return pipe.readFrom(source)
        .withNativeTimestamps(0)
        .setLocalParallelism(1)
        .setName("Convert content")
        .map(Map.Entry::getValue)
        .map(this.databaseAdapter.getRecordConverter()::convert);
  }
}
