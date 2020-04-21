package uc3.streamprocessing;

import com.google.common.math.Stats;
import java.time.Duration;
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
import titan.ccp.common.kafka.GenericSerde;
import titan.ccp.common.kieker.kafka.IMonitoringRecordSerde;
import titan.ccp.models.records.ActivePowerRecordFactory;
import uc3.streamprocessing.util.StatsFactory;

/**
 * Builds Kafka Stream Topology for the History microservice.
 */
public class TopologyBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopologyBuilder.class);

  private final String inputTopic;
  private final String outputTopic;
  private final Duration duration;

  private final StreamsBuilder builder = new StreamsBuilder();

  /**
   * Create a new {@link TopologyBuilder} using the given topics.
   */
  public TopologyBuilder(final String inputTopic, final String outputTopic,
      final Duration duration) {
    this.inputTopic = inputTopic;
    this.outputTopic = outputTopic;
    this.duration = duration;
  }

  /**
   * Build the {@link Topology} for the History microservice.
   */
  public Topology build() {
    this.builder
        .stream(this.inputTopic,
            Consumed.with(Serdes.String(),
                IMonitoringRecordSerde.serde(new ActivePowerRecordFactory())))
        .groupByKey().windowedBy(TimeWindows.of(this.duration))
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
        .peek((k, v) -> System.out.printf("key %s, value %f \n", k, v))
        .to(this.outputTopic, Produced.with(Serdes.String(), Serdes.String()));

    return this.builder.build();
  }
}
