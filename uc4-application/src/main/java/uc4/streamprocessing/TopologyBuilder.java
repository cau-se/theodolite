package uc4.streamprocessing;

import com.google.common.math.Stats;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import titan.ccp.common.kafka.GenericSerde;
import titan.ccp.common.kieker.kafka.IMonitoringRecordSerde;
import titan.ccp.models.records.ActivePowerRecordFactory;
import uc4.streamprocessing.util.StatsFactory;

/**
 * Builds Kafka Stream Topology for the History microservice.
 */
public class TopologyBuilder {

  // private static final Logger LOGGER = LoggerFactory.getLogger(TopologyBuilder.class);

  private final ZoneId zone = ZoneId.of("Europe/Paris"); // TODO as parameter


  private final String inputTopic;
  private final String outputTopic;
  private final Duration aggregtionDuration;
  private final Duration aggregationAdvance;

  private final StreamsBuilder builder = new StreamsBuilder();

  /**
   * Create a new {@link TopologyBuilder} using the given topics.
   */
  public TopologyBuilder(final String inputTopic, final String outputTopic,
      final Duration aggregtionDuration, final Duration aggregationAdvance) {
    this.inputTopic = inputTopic;
    this.outputTopic = outputTopic;
    this.aggregtionDuration = aggregtionDuration;
    this.aggregationAdvance = aggregationAdvance;
  }

  /**
   * Build the {@link Topology} for the History microservice.
   */
  public Topology build() {

    final StatsKeyFactory<HourOfDayKey> keyFactory = new HourOfDayKeyFactory();
    final Serde<HourOfDayKey> keySerde = HourOfDayKeySerde.create();
    // final StatsRecordFactory<HourOfDayKey, HourOfDayActivePowerRecord> statsRecordFactory = new
    // HourOfDayRecordFactory();

    this.builder
        .stream(this.inputTopic,
            Consumed.with(Serdes.String(),
                IMonitoringRecordSerde.serde(new ActivePowerRecordFactory())))
        .selectKey((key, value) -> {
          final Instant instant = Instant.ofEpochMilli(value.getTimestamp());
          final LocalDateTime dateTime = LocalDateTime.ofInstant(instant, this.zone);
          return keyFactory.createKey(value.getIdentifier(), dateTime);
        })
        .groupByKey(
            Grouped.with(keySerde, IMonitoringRecordSerde.serde(new ActivePowerRecordFactory())))
        .windowedBy(TimeWindows.of(this.aggregtionDuration).advanceBy(this.aggregationAdvance))
        .aggregate(
            () -> Stats.of(),
            (k, record, stats) -> StatsFactory.accumulate(stats, record.getValueInW()),
            Materialized.with(keySerde,
                GenericSerde.from(Stats::toByteArray, Stats::fromByteArray)))
        .toStream()
        .map((key, stats) -> KeyValue.pair(
            keyFactory.getSensorId(key.key()),
            stats.toString()))
        // TODO
        // statsRecordFactory.create(key, value)))
        // .peek((k, v) -> LOGGER.info("{}: {}", k, v)) // TODO Temp logging
        .to(
            this.outputTopic,
            Produced.with(
                Serdes.String(),
                Serdes.String()));
    // this.serdes.avroValues()));

    return this.builder.build();
  }
}
