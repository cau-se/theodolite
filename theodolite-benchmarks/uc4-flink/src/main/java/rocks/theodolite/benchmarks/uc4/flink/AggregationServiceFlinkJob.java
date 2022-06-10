package rocks.theodolite.benchmarks.uc4.flink; // NOPMD Imports required

import java.time.Duration;
import java.util.Set;
import org.apache.commons.configuration2.Configuration;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumerBase;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.theodolite.benchmarks.commons.commons.configuration.ServiceConfigurations;
import rocks.theodolite.benchmarks.commons.configuration.events.Event;
import rocks.theodolite.benchmarks.commons.configuration.events.EventSerde;
import rocks.theodolite.benchmarks.commons.flink.KafkaConnectorFactory;
import rocks.theodolite.benchmarks.commons.flink.StateBackends;
import rocks.theodolite.benchmarks.commons.flink.TupleType;
import rocks.theodolite.benchmarks.commons.kafka.avro.SchemaRegistryAvroSerdeFactory;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;
import rocks.theodolite.benchmarks.commons.model.records.AggregatedActivePowerRecord;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.ImmutableSensorRegistry;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.SensorRegistry;
import rocks.theodolite.benchmarks.uc4.flink.util.ImmutableSensorRegistrySerializer;
import rocks.theodolite.benchmarks.uc4.flink.util.ImmutableSetSerializer;
import rocks.theodolite.benchmarks.uc4.flink.util.SensorParentKey;
import rocks.theodolite.benchmarks.uc4.flink.util.SensorParentKeySerializer;

/**
 * The Aggregation microservice implemented as a Flink job.
 */
public final class AggregationServiceFlinkJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(AggregationServiceFlinkJob.class);

  private final Configuration config = ServiceConfigurations.createWithDefaults();
  private final StreamExecutionEnvironment env;
  private final String applicationId;

  /**
   * Create a new {@link AggregationServiceFlinkJob}.
   */
  public AggregationServiceFlinkJob() {
    final String applicationName = this.config.getString(ConfigurationKeys.APPLICATION_NAME);
    final String applicationVersion = this.config.getString(ConfigurationKeys.APPLICATION_VERSION);
    this.applicationId = applicationName + "-" + applicationVersion;

    // Execution environment configuration
    // org.apache.flink.configuration.Configuration conf = new
    // org.apache.flink.configuration.Configuration();
    // conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true);
    // final StreamExecutionEnvironment env =
    // StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
    this.env = StreamExecutionEnvironment.getExecutionEnvironment();

    this.configureEnv();

    this.buildPipeline();
  }

  private void configureEnv() {
    final boolean checkpointing = this.config.getBoolean(ConfigurationKeys.CHECKPOINTING, true);
    final int commitIntervalMs = this.config.getInt(ConfigurationKeys.COMMIT_INTERVAL_MS);
    if (checkpointing) {
      this.env.enableCheckpointing(commitIntervalMs);
    }

    // Parallelism
    final Integer parallelism = this.config.getInteger(ConfigurationKeys.PARALLELISM, null);
    if (parallelism != null) {
      LOGGER.info("Set parallelism: {}.", parallelism);
      this.env.setParallelism(parallelism);
    }

    // State Backend
    final StateBackend stateBackend = StateBackends.fromConfiguration(this.config);
    this.env.setStateBackend(stateBackend);

    this.configureSerializers();
  }

  private void configureSerializers() {
    this.env.getConfig().registerTypeWithKryoSerializer(ImmutableSensorRegistry.class,
        new ImmutableSensorRegistrySerializer());
    this.env.getConfig().registerTypeWithKryoSerializer(SensorParentKey.class,
        new SensorParentKeySerializer());

    this.env.getConfig().registerTypeWithKryoSerializer(Set.of().getClass(),
        new ImmutableSetSerializer());
    this.env.getConfig().registerTypeWithKryoSerializer(Set.of(1).getClass(),
        new ImmutableSetSerializer());
    this.env.getConfig().registerTypeWithKryoSerializer(Set.of(1, 2, 3, 4).getClass(), // NOCS
        new ImmutableSetSerializer());

    this.env
        .getConfig()
        .getRegisteredTypesWithKryoSerializers()
        .forEach((c, s) -> LOGGER.info("Class '{}' registered with serializer '{}'.", c.getName(),
            s.getSerializer().getClass().getName()));
  }

  private void buildPipeline() {
    // Get configurations
    final String kafkaBroker = this.config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS);
    final String schemaRegistryUrl = this.config.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL);
    final String inputTopic = this.config.getString(ConfigurationKeys.KAFKA_INPUT_TOPIC);
    final String outputTopic = this.config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC);
    final Time windowSize =
        Time.milliseconds(this.config.getLong(ConfigurationKeys.WINDOW_SIZE_MS));
    final Duration windowGrace =
        Duration.ofMillis(this.config.getLong(ConfigurationKeys.WINDOW_GRACE_MS));
    final String configurationTopic =
        this.config.getString(ConfigurationKeys.CONFIGURATION_KAFKA_TOPIC);
    final boolean checkpointing = this.config.getBoolean(ConfigurationKeys.CHECKPOINTING, true);

    final KafkaConnectorFactory kafkaConnector = new KafkaConnectorFactory(
        this.applicationId, kafkaBroker, checkpointing, schemaRegistryUrl);

    // Source from input topic with ActivePowerRecords
    final FlinkKafkaConsumer<ActivePowerRecord> kafkaInputSource =
        kafkaConnector.createConsumer(inputTopic, ActivePowerRecord.class);
    // TODO Watermarks?

    // Source from output topic with AggregatedPowerRecords
    final FlinkKafkaConsumer<AggregatedActivePowerRecord> kafkaOutputSource =
        kafkaConnector.createConsumer(outputTopic, AggregatedActivePowerRecord.class);

    final FlinkKafkaConsumerBase<Tuple2<Event, String>> kafkaConfigSource =
        kafkaConnector.createConsumer(
            configurationTopic,
            EventSerde::serde,
            Serdes::String,
            TupleType.of(TypeInformation.of(Event.class), Types.STRING))
            .setStartFromEarliest();

    // Sink to output topic with SensorId, AggregatedActivePowerRecord
    final FlinkKafkaProducer<Tuple2<String, AggregatedActivePowerRecord>> kafkaAggregationSink =
        kafkaConnector.createProducer(
            outputTopic,
            Serdes::String,
            () -> new SchemaRegistryAvroSerdeFactory(schemaRegistryUrl).forValues(),
            Types.TUPLE(Types.STRING, TypeInformation.of(AggregatedActivePowerRecord.class)));

    // Build input stream
    final DataStream<ActivePowerRecord> inputStream = this.env.addSource(kafkaInputSource)
        .name("[Kafka Consumer] Topic: " + inputTopic)// NOCS
        // .rebalance()
        .map(r -> r)
        .name("[Map] Rebalance Forward");

    // Build aggregation stream
    final DataStream<ActivePowerRecord> aggregationsInputStream =
        this.env.addSource(kafkaOutputSource)
            .name("[Kafka Consumer] Topic: " + outputTopic) // NOCS
            // .rebalance()
            .map(r -> new ActivePowerRecord(r.getIdentifier(), r.getTimestamp(), r.getSumInW()))
            .name("[Map] AggregatedActivePowerRecord -> ActivePowerRecord");

    // Merge input and aggregation streams
    final DataStream<ActivePowerRecord> mergedInputStream = inputStream
        .union(aggregationsInputStream);

    // Build parent sensor stream from configuration stream
    final DataStream<Tuple2<String, Set<String>>> configurationsStream =
        this.env.addSource(kafkaConfigSource)
            .name("[Kafka Consumer] Topic: " + configurationTopic) // NOCS
            .filter(tuple -> tuple.f0 == Event.SENSOR_REGISTRY_CHANGED
                || tuple.f0 == Event.SENSOR_REGISTRY_STATUS)
            .name("[Filter] SensorRegistry changed")
            .map(tuple -> SensorRegistry.fromJson(tuple.f1)).name("[Map] JSON -> SensorRegistry")
            .keyBy(sr -> 1)
            .flatMap(new ChildParentsFlatMapFunction())
            .name("[FlatMap] SensorRegistry -> (ChildSensor, ParentSensor[])");

    final DataStream<Tuple2<SensorParentKey, ActivePowerRecord>> lastValueStream =
        mergedInputStream.connect(configurationsStream)
            .keyBy(ActivePowerRecord::getIdentifier,
                (KeySelector<Tuple2<String, Set<String>>, String>) t -> t.f0)
            .flatMap(new JoinAndDuplicateCoFlatMapFunction())
            .name("[CoFlatMap] Join input-config, Flatten to ((Sensor, Group), ActivePowerRecord)");

    final DataStream<AggregatedActivePowerRecord> aggregationStream = lastValueStream
        .rebalance()
        .assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(windowGrace))
        .keyBy(t -> t.f0.getParent())
        .window(TumblingEventTimeWindows.of(windowSize))
        .process(new RecordAggregationProcessWindowFunction())
        .name("[Aggregate] ((Sensor, Group), ActivePowerRecord) -> AggregatedActivePowerRecord");

    // add Kafka Sink
    aggregationStream
        .map(value -> new Tuple2<>(value.getIdentifier(), value))
        .name("[Map] AggregatedActivePowerRecord -> (Sensor, AggregatedActivePowerRecord)")
        .returns(Types.TUPLE(Types.STRING, TypeInformation.of(AggregatedActivePowerRecord.class)))
        .addSink(kafkaAggregationSink).name("[Kafka Producer] Topic: " + outputTopic);
  }

  /**
   * Start running this microservice.
   */
  public void run() {
    // Execution plan
    LOGGER.info("Execution plan: {}", this.env.getExecutionPlan());

    // Execute Job
    try {
      this.env.execute(this.applicationId);
    } catch (final Exception e) { // NOPMD Execution thrown by Flink
      LOGGER.error("An error occured while running this job.", e);
    }
  }

  public static void main(final String[] args) {
    new AggregationServiceFlinkJob().run();
  }
}
