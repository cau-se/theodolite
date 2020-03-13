package titan.ccp.aggregation.streamprocessing;

import com.google.common.math.StatsAccumulator;
import java.time.Duration;
import java.util.Set;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
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
import titan.ccp.common.kieker.kafka.IMonitoringRecordSerde;
import titan.ccp.configuration.events.Event;
import titan.ccp.configuration.events.EventSerde;
import titan.ccp.model.sensorregistry.SensorRegistry;
import titan.ccp.models.records.ActivePowerRecord;
import titan.ccp.models.records.ActivePowerRecordFactory;
import titan.ccp.models.records.AggregatedActivePowerRecord;
import titan.ccp.models.records.AggregatedActivePowerRecordFactory;

/**
 * Builds Kafka Stream Topology for the History microservice.
 */
public class TopologyBuilder {

  // private static final Logger LOGGER = LoggerFactory.getLogger(TopologyBuilder.class);

  private final String inputTopic;
  private final String outputTopic;
  private final String configurationTopic;
  private final Duration windowSize;
  private final Duration gracePeriod;

  private final StreamsBuilder builder = new StreamsBuilder();
  private final RecordAggregator recordAggregator = new RecordAggregator();


  /**
   * Create a new {@link TopologyBuilder} using the given topics.
   */
  public TopologyBuilder(final String inputTopic, final String outputTopic,
      final String configurationTopic, final Duration windowSize, final Duration gracePeriod) {
    this.inputTopic = inputTopic;
    this.outputTopic = outputTopic;
    this.configurationTopic = configurationTopic;
    this.windowSize = windowSize;
    this.gracePeriod = gracePeriod;
  }

  /**
   * Build the {@link Topology} for the History microservice.
   */
  public Topology build() {
    // 1. Build Parent-Sensor Table
    final KTable<String, Set<String>> parentSensorTable = this.buildParentSensorTable();

    // 2. Build Input Table
    final KTable<String, ActivePowerRecord> inputTable = this.buildInputTable();

    // 3. Build Last Value Table from Input and Parent-Sensor Table
    final KTable<Windowed<SensorParentKey>, ActivePowerRecord> lastValueTable =
        this.buildLastValueTable(parentSensorTable, inputTable);

    // 4. Build Aggregations Stream
    final KStream<String, AggregatedActivePowerRecord> aggregations =
        this.buildAggregationStream(lastValueTable);

    // 5. Expose Aggregations Stream
    this.exposeOutputStream(aggregations);

    return this.builder.build();
  }

  private KTable<String, ActivePowerRecord> buildInputTable() {
    final KStream<String, ActivePowerRecord> values = this.builder
        .stream(this.inputTopic, Consumed.with(
            Serdes.String(),
            IMonitoringRecordSerde.serde(new ActivePowerRecordFactory())));
    final KStream<String, ActivePowerRecord> aggregationsInput = this.builder
        .stream(this.outputTopic, Consumed.with(
            Serdes.String(),
            IMonitoringRecordSerde.serde(new AggregatedActivePowerRecordFactory())))
        .mapValues(r -> new ActivePowerRecord(r.getIdentifier(), r.getTimestamp(), r.getSumInW()));

    final KTable<String, ActivePowerRecord> inputTable = values
        .merge(aggregationsInput)
        .mapValues((k, v) -> new ActivePowerRecord(v.getIdentifier(), System.currentTimeMillis(),
            v.getValueInW()))
        .groupByKey(Grouped.with(Serdes.String(),
            IMonitoringRecordSerde.serde(new ActivePowerRecordFactory())))
        .reduce((aggr, value) -> value, Materialized.with(Serdes.String(),
            IMonitoringRecordSerde.serde(new ActivePowerRecordFactory())));
    return inputTable;
  }

  private KTable<String, Set<String>> buildParentSensorTable() {
    final KStream<Event, String> configurationStream = this.builder
        .stream(this.configurationTopic, Consumed.with(EventSerde.serde(), Serdes.String()))
        .filter((key, value) -> key == Event.SENSOR_REGISTRY_CHANGED
            || key == Event.SENSOR_REGISTRY_STATUS);

    final ChildParentsTransformerFactory childParentsTransformerFactory =
        new ChildParentsTransformerFactory();
    this.builder.addStateStore(childParentsTransformerFactory.getStoreBuilder());

    return configurationStream
        .mapValues(data -> SensorRegistry.fromJson(data))
        .flatTransform(
            childParentsTransformerFactory.getTransformerSupplier(),
            childParentsTransformerFactory.getStoreName())
        .groupByKey(Grouped.with(Serdes.String(), OptionalParentsSerde.serde()))
        .aggregate(
            () -> Set.<String>of(),
            (key, newValue, oldValue) -> newValue.orElse(null),
            Materialized.with(Serdes.String(), ParentsSerde.serde()));
  }


  private KTable<Windowed<SensorParentKey>, ActivePowerRecord> buildLastValueTable(
      final KTable<String, Set<String>> parentSensorTable,
      final KTable<String, ActivePowerRecord> inputTable) {
    final JointFlatTransformerFactory jointFlatMapTransformerFactory =
        new JointFlatTransformerFactory();
    this.builder.addStateStore(jointFlatMapTransformerFactory.getStoreBuilder());

    return inputTable
        .join(parentSensorTable, (record, parents) -> new JointRecordParents(parents, record))
        .toStream()
        .flatTransform(
            jointFlatMapTransformerFactory.getTransformerSupplier(),
            jointFlatMapTransformerFactory.getStoreName())
        .groupByKey(Grouped.with(
            SensorParentKeySerde.serde(),
            IMonitoringRecordSerde.serde(new ActivePowerRecordFactory())))
        .windowedBy(TimeWindows.of(this.windowSize).grace(this.gracePeriod))
        .reduce(
            // TODO Configurable window aggregation function
            (aggValue, newValue) -> newValue,
            Materialized.with(SensorParentKeySerde.serde(),
                IMonitoringRecordSerde.serde(new ActivePowerRecordFactory())));

  }

  private KStream<String, AggregatedActivePowerRecord> buildAggregationStream(
      final KTable<Windowed<SensorParentKey>, ActivePowerRecord> lastValueTable) {
    return lastValueTable
        .groupBy(
            (k, v) -> KeyValue.pair(new Windowed<>(k.key().getParent(), k.window()), v),
            Grouped.with(
                new WindowedSerdes.TimeWindowedSerde<>(
                    Serdes.String(),
                    this.windowSize.toMillis()),
                IMonitoringRecordSerde.serde(new ActivePowerRecordFactory())))
        .aggregate(
            () -> null, this.recordAggregator::add, this.recordAggregator::substract,
            Materialized.with(
                new WindowedSerdes.TimeWindowedSerde<>(
                    Serdes.String(),
                    this.windowSize.toMillis()),
                IMonitoringRecordSerde.serde(new AggregatedActivePowerRecordFactory())))
        .suppress(Suppressed.untilTimeLimit(this.windowSize, BufferConfig.unbounded()))
        // .suppress(Suppressed.untilWindowCloses(BufferConfig.unbounded()))
        .toStream()
        // TODO timestamp -1 indicates that this record is emitted by an substract event
        .filter((k, record) -> record.getTimestamp() != -1)
        .map((k, v) -> KeyValue.pair(k.key(), v)); // TODO compute Timestamp
  }

  private StatsAccumulator latencyStats = new StatsAccumulator();
  private long lastTime = System.currentTimeMillis();

  private void exposeOutputStream(final KStream<String, AggregatedActivePowerRecord> aggregations) {
    aggregations
        .peek((k, v) -> {
          final long time = System.currentTimeMillis();
          final long latency = time - v.getTimestamp();
          this.latencyStats.add(latency);
          if (time - this.lastTime >= 1000) {
            System.out.println("latency,"
                + time + ','
                + this.latencyStats.mean() + ','
                + (this.latencyStats.count() > 0
                    ? this.latencyStats.populationStandardDeviation()
                    : Double.NaN)
                + ','
                + (this.latencyStats.count() > 1
                    ? this.latencyStats.sampleStandardDeviation()
                    : Double.NaN)
                + ','
                + this.latencyStats.min() + ','
                + this.latencyStats.max() + ','
                + this.latencyStats.count());
            this.latencyStats = new StatsAccumulator();
            this.lastTime = time;
          }
        })
        .to(this.outputTopic, Produced.with(
            Serdes.String(),
            IMonitoringRecordSerde.serde(new AggregatedActivePowerRecordFactory())));
  }
}
