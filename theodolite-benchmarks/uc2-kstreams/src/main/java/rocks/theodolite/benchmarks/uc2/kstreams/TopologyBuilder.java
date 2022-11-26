package rocks.theodolite.benchmarks.uc2.kstreams;

import com.google.common.math.Stats;
import java.time.Duration;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import rocks.theodolite.benchmarks.commons.kafka.avro.SchemaRegistryAvroSerdeFactory;
import rocks.theodolite.benchmarks.commons.kstreams.GenericSerde;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;
import rocks.theodolite.benchmarks.uc2.kstreams.util.StatsFactory;

/**
 * Builds Kafka Stream Topology for the History microservice.
 */
public class TopologyBuilder {

  private final String inputTopic;
  private final String outputTopic;
  private final SchemaRegistryAvroSerdeFactory srAvroSerdeFactory;
  private final Duration duration;

  private final StreamsBuilder builder = new StreamsBuilder();

  /**
   * Create a new {@link TopologyBuilder} using the given topics.
   */
  public TopologyBuilder(final String inputTopic, final String outputTopic,
      final SchemaRegistryAvroSerdeFactory srAvroSerdeFactory,
      final Duration duration) {
    this.inputTopic = inputTopic;
    this.outputTopic = outputTopic;
    this.srAvroSerdeFactory = srAvroSerdeFactory;
    this.duration = duration;
  }

  /**
   * Build the {@link Topology} for the History microservice.
   */
  public Topology build(final Properties properties) {
    this.builder
        .stream(this.inputTopic,
            Consumed.with(Serdes.String(),
                this.srAvroSerdeFactory.<ActivePowerRecord>forValues()))
        .groupByKey()
        .windowedBy(TimeWindows.ofSizeWithNoGrace(this.duration))
        .aggregate(
            () -> Stats.of(),
            (k, record, stats) -> StatsFactory.accumulate(stats, record.getValueInW()),
            Materialized.with(
                Serdes.String(),
                GenericSerde.from(Stats::toByteArray, Stats::fromByteArray)))
        .toStream()
        .map((k, s) -> KeyValue.pair(k.key(), s.toString()))
        // .peek((k, v) -> LOGGER.info(k + ": " + v))
        .to(this.outputTopic, Produced.with(Serdes.String(), Serdes.String()));

    return this.builder.build(properties);
  }
}
