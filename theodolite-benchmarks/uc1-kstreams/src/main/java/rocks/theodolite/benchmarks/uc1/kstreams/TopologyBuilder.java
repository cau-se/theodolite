package rocks.theodolite.benchmarks.uc1.kstreams;

import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import rocks.theodolite.benchmarks.commons.kafka.avro.SchemaRegistryAvroSerdeFactory;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseAdapter;
import rocks.theodolite.benchmarks.uc1.commons.logger.LogWriterFactory;

/**
 * Builds Kafka Stream Topology for the History microservice.
 */
public class TopologyBuilder {

  private final String inputTopic;
  private final SchemaRegistryAvroSerdeFactory srAvroSerdeFactory;

  private final DatabaseAdapter<String> databaseAdapter = LogWriterFactory.forJson();

  private final StreamsBuilder builder = new StreamsBuilder();


  /**
   * Create a new {@link TopologyBuilder} using the given topics.
   */
  public TopologyBuilder(final String inputTopic,
      final SchemaRegistryAvroSerdeFactory srAvroSerdeFactory) {
    this.inputTopic = inputTopic;
    this.srAvroSerdeFactory = srAvroSerdeFactory;
  }

  /**
   * Build the {@link Topology} for the History microservice.
   */
  public Topology build(final Properties properties) {
    this.builder
        .stream(this.inputTopic, Consumed.with(
            Serdes.String(),
            this.srAvroSerdeFactory.<ActivePowerRecord>forValues()))
        .mapValues(this.databaseAdapter.getRecordConverter()::convert)
        .foreach((k, record) -> this.databaseAdapter.getDatabaseWriter().write(record));

    return this.builder.build(properties);
  }
}
