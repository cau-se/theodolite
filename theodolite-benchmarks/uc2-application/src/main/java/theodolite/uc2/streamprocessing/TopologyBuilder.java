package theodolite.uc2.streamprocessing;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theodolite.uc2.streamprocessing.util.StatsFactory;
import titan.ccp.common.kafka.GenericSerde;
import titan.ccp.common.kafka.avro.SchemaRegistryAvroSerdeFactory;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Builds Kafka Stream Topology for the History microservice.
 */
public class TopologyBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopologyBuilder.class);

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
        .windowedBy(TimeWindows.of(this.duration))
        // .aggregate(
        // () -> 0.0,
        // (key, activePowerRecord, agg) -> agg + activePowerRecord.getValueInW(),
        // Materialized.with(Serdes.String(), Serdes.Double()))
        .aggregate(
            () -> Stats.of(),
            (k, record, stats) -> StatsFactory.accumulate(stats, record.getValueInW()),
            Materialized.with(
                Serdes.String(),
                GenericSerde.from(Stats::toByteArray, Stats::fromByteArray)))
        .toStream()
        .map((k, s) -> KeyValue.pair(k.key(), s.toString()))
        .peek((k, v) -> LOGGER.info(k + ": " + v))
        .to(this.outputTopic, Produced.with(Serdes.String(), Serdes.String()));

    return this.builder.build(properties);
  }
}
