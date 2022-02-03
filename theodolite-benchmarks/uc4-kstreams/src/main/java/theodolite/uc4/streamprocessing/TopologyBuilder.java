package theodolite.uc4.streamprocessing;

import java.time.Duration;
import java.util.Properties;
import java.util.Set;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.Topology.AutoOffsetReset;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.Suppressed.BufferConfig;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.WindowedSerdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.common.kafka.avro.SchemaRegistryAvroSerdeFactory;
import titan.ccp.configuration.events.Event;
import titan.ccp.configuration.events.EventSerde;
import titan.ccp.model.records.ActivePowerRecord;
import titan.ccp.model.records.AggregatedActivePowerRecord;
import titan.ccp.model.sensorregistry.SensorRegistry;

/**
 * Builds Kafka Stream Topology for the History microservice.
 */
public class TopologyBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopologyBuilder.class);

  // Streams Variables
  private final String inputTopic;
  private final String feedbackTopic;
  private final String outputTopic;
  private final String configurationTopic;
  private final Duration emitPeriod;
  private final Duration gracePeriod;

  // Serdes
  private final SchemaRegistryAvroSerdeFactory srAvroSerdeFactory;

  private final StreamsBuilder builder = new StreamsBuilder();
  private final RecordAggregator recordAggregator = new RecordAggregator();

  /**
   * Create a new {@link TopologyBuilder} using the given topics.
   *
   * @param inputTopic The topic where to read sensor measurements from.
   * @param configurationTopic The topic where the hierarchy of the sensors is published.
   * @param feedbackTopic The topic where aggregation results are written to for feedback.
   * @param outputTopic The topic where to publish aggregation results.
   * @param emitPeriod The Duration results are emitted with.
   * @param gracePeriod The Duration for how long late arriving records are considered.
   * @param srAvroSerdeFactory Factory for creating avro SERDEs
   *
   */
  public TopologyBuilder(final String inputTopic, final String outputTopic,
      final String feedbackTopic, final String configurationTopic,
      final Duration emitPeriod, final Duration gracePeriod,
      final SchemaRegistryAvroSerdeFactory srAvroSerdeFactory) {
    this.inputTopic = inputTopic;
    this.outputTopic = outputTopic;
    this.feedbackTopic = feedbackTopic;
    this.configurationTopic = configurationTopic;
    this.emitPeriod = emitPeriod;
    this.gracePeriod = gracePeriod;

    this.srAvroSerdeFactory = srAvroSerdeFactory;
  }

  /**
   * Build the {@link Topology} for the Aggregation microservice.
   */
  public Topology build(final Properties properties) {
    // 1. Build Parent-Sensor Table
    final KTable<String, Set<String>> parentSensorTable = this.buildParentSensorTable();

    // 2. Build Input Table
    final KTable<String, ActivePowerRecord> inputTable = this.buildInputTable();

    // 3. Build Last Value Table from Input and Parent-Sensor Table
    final KTable<Windowed<SensorParentKey>, ActivePowerRecord> lastValueTable =
        this.buildLastValueTable(parentSensorTable, inputTable);

    // 4. Build Aggregations Stream
    final KTable<Windowed<String>, AggregatedActivePowerRecord> aggregations =
        this.buildAggregationStream(lastValueTable);

    // 6. Expose Feedback Stream
    this.exposeFeedbackStream(aggregations);

    // 5. Expose Aggregations Stream
    this.exposeOutputStream(aggregations);

    return this.builder.build(properties);
  }

  private KTable<String, ActivePowerRecord> buildInputTable() {
    final KStream<String, ActivePowerRecord> values = this.builder
        .stream(this.inputTopic, Consumed.with(
            Serdes.String(),
            this.srAvroSerdeFactory.forValues()));

    final KStream<String, ActivePowerRecord> aggregationsInput = this.builder
        .stream(this.feedbackTopic, Consumed.with(
            Serdes.String(),
            this.srAvroSerdeFactory.<AggregatedActivePowerRecord>forValues()))
        .mapValues(r -> new ActivePowerRecord(r.getIdentifier(), r.getTimestamp(), r.getSumInW()));

    final KTable<String, ActivePowerRecord> inputTable = values
        .merge(aggregationsInput)
        .groupByKey(Grouped.with(
            Serdes.String(),
            this.srAvroSerdeFactory.forValues()))
        .reduce((aggr, value) -> value, Materialized.with(
            Serdes.String(),
            this.srAvroSerdeFactory.forValues()));
    return inputTable;
  }

  private KTable<String, Set<String>> buildParentSensorTable() {
    final KStream<Event, String> configurationStream = this.builder
        .stream(this.configurationTopic, Consumed
            .with(EventSerde.serde(), Serdes.String())
            .withOffsetResetPolicy(AutoOffsetReset.EARLIEST))
        .peek((key, value) -> LOGGER.info("Received event: '{}'->'{}'", key, value))
        .filter((key, value) -> key == Event.SENSOR_REGISTRY_CHANGED
            || key == Event.SENSOR_REGISTRY_STATUS);

    return configurationStream
        .mapValues(data -> SensorRegistry.fromJson(data))
        .flatTransform(new ChildParentsTransformerSupplier())
        .groupByKey(Grouped.with(Serdes.String(), OptionalParentsSerde.serde()))
        .aggregate(
            () -> Set.<String>of(),
            (key, newValue, oldValue) -> newValue.orElse(null),
            Materialized.with(Serdes.String(), ParentsSerde.serde()));
  }

  private KTable<Windowed<SensorParentKey>, ActivePowerRecord> buildLastValueTable(
      final KTable<String, Set<String>> parentSensorTable,
      final KTable<String, ActivePowerRecord> inputTable) {

    return inputTable
        .join(parentSensorTable, (record, parents) -> new JointRecordParents(parents, record))
        .toStream()
        .flatTransform(new JointFlatTransformerSupplier())
        .groupByKey(Grouped.with(
            SensorParentKeySerde.serde(),
            this.srAvroSerdeFactory.forValues()))
        .windowedBy(TimeWindows.of(this.emitPeriod).grace(this.gracePeriod))
        .reduce(
            // TODO Configurable window aggregation function
            (oldVal, newVal) -> newVal.getTimestamp() >= oldVal.getTimestamp() ? newVal : oldVal,
            Materialized.with(
                SensorParentKeySerde.serde(),
                this.srAvroSerdeFactory.forValues()));
  }

  private KTable<Windowed<String>, AggregatedActivePowerRecord> buildAggregationStream(
      final KTable<Windowed<SensorParentKey>, ActivePowerRecord> lastValueTable) {
    return lastValueTable
        .groupBy(
            (k, v) -> KeyValue.pair(new Windowed<>(k.key().getParent(), k.window()), v),
            Grouped.with(
                new WindowedSerdes.TimeWindowedSerde<>(
                    Serdes.String(),
                    this.emitPeriod.toMillis()),
                this.srAvroSerdeFactory.forValues()))
        .aggregate(
            () -> null,
            this.recordAggregator::add,
            this.recordAggregator::substract,
            Materialized.with(
                new WindowedSerdes.TimeWindowedSerde<>(
                    Serdes.String(),
                    this.emitPeriod.toMillis()),
                this.srAvroSerdeFactory.forValues()))
        // TODO timestamp -1 indicates that this record is emitted by an substract event
        .filter((k, record) -> record.getTimestamp() != -1);
  }

  private void exposeFeedbackStream(
      final KTable<Windowed<String>, AggregatedActivePowerRecord> aggregations) {

    aggregations
        .toStream()
        .filter((k, record) -> record != null)
        .selectKey((k, v) -> k.key())
        .to(this.feedbackTopic, Produced.with(
            Serdes.String(),
            this.srAvroSerdeFactory.forValues()));
  }

  private void exposeOutputStream(
      final KTable<Windowed<String>, AggregatedActivePowerRecord> aggregations) {

    aggregations
        // .suppress(Suppressed.untilWindowCloses(BufferConfig.unbounded()))
        .suppress(Suppressed.untilTimeLimit(this.emitPeriod, BufferConfig.unbounded()))
        .toStream()
        .filter((k, record) -> record != null)
        .selectKey((k, v) -> k.key())
        .to(this.outputTopic, Produced.with(
            Serdes.String(),
            this.srAvroSerdeFactory.forValues()));
  }
}
