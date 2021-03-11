package theodolite.uc4.application;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.configuration2.Configuration;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.runtime.state.memory.MemoryStateBackend;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theodolite.commons.flink.serialization.FlinkKafkaKeyValueSerde;
import theodolite.uc4.application.util.ImmutableSensorRegistrySerializer;
import theodolite.uc4.application.util.ImmutableSetSerializer;
import theodolite.uc4.application.util.SensorParentKey;
import theodolite.uc4.application.util.SensorParentKeySerializer;
import titan.ccp.common.configuration.ServiceConfigurations;
import titan.ccp.common.kafka.avro.SchemaRegistryAvroSerdeFactory;
import titan.ccp.configuration.events.Event;
import titan.ccp.configuration.events.EventSerde;
import titan.ccp.model.records.ActivePowerRecord;
import titan.ccp.model.records.AggregatedActivePowerRecord;
import titan.ccp.model.sensorregistry.ImmutableSensorRegistry;
import titan.ccp.model.sensorregistry.SensorRegistry;

/**
 * The Aggregation microservice implemented as a Flink job.
 */
public class AggregationServiceFlinkJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(AggregationServiceFlinkJob.class);

  private final Configuration config = ServiceConfigurations.createWithDefaults();

  private void run() {
    // Configurations
    final String applicationName = this.config.getString(ConfigurationKeys.APPLICATION_NAME);
    final String applicationVersion = this.config.getString(ConfigurationKeys.APPLICATION_VERSION);
    final String applicationId = applicationName + "-" + applicationVersion;
    final int commitIntervalMs = this.config.getInt(ConfigurationKeys.COMMIT_INTERVAL_MS);
    final String kafkaBroker = this.config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS);
    final String inputTopic = this.config.getString(ConfigurationKeys.KAFKA_INPUT_TOPIC);
    final String outputTopic = this.config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC);
    final String schemaRegistryUrl = this.config.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL);
    final Time windowSize =
        Time.milliseconds(this.config.getLong(ConfigurationKeys.WINDOW_SIZE_MS));
    final Duration windowGrace =
        Duration.ofMillis(this.config.getLong(ConfigurationKeys.WINDOW_GRACE_MS));
    final String configurationTopic =
        this.config.getString(ConfigurationKeys.CONFIGURATION_KAFKA_TOPIC);
    final String stateBackend =
        this.config.getString(ConfigurationKeys.FLINK_STATE_BACKEND, "").toLowerCase();
    final String stateBackendPath = this.config
        .getString(ConfigurationKeys.FLINK_STATE_BACKEND_PATH, "/opt/flink/statebackend");
    final int memoryStateBackendSize =
        this.config.getInt(ConfigurationKeys.FLINK_STATE_BACKEND_MEMORY_SIZE,
            MemoryStateBackend.DEFAULT_MAX_STATE_SIZE);
    final boolean debug = this.config.getBoolean(ConfigurationKeys.DEBUG, true);
    final boolean checkpointing = this.config.getBoolean(ConfigurationKeys.CHECKPOINTING, true);

    final Properties kafkaProps = new Properties();
    kafkaProps.setProperty("bootstrap.servers", kafkaBroker);
    kafkaProps.setProperty("group.id", applicationId);

    // Sources and Sinks with Serializer and Deserializer

    // Source from input topic with ActivePowerRecords
    final DeserializationSchema<ActivePowerRecord> inputSerde =
        ConfluentRegistryAvroDeserializationSchema.forSpecific(
            ActivePowerRecord.class,
            schemaRegistryUrl);

    final FlinkKafkaConsumer<ActivePowerRecord> kafkaInputSource = new FlinkKafkaConsumer<>(
        inputTopic, inputSerde, kafkaProps);

    kafkaInputSource.setStartFromGroupOffsets();
    if (checkpointing) {
      kafkaInputSource.setCommitOffsetsOnCheckpoints(true);
    }

    // Source from output topic with AggregatedPowerRecords
    final DeserializationSchema<AggregatedActivePowerRecord> outputSerde =
        ConfluentRegistryAvroDeserializationSchema.forSpecific(
            AggregatedActivePowerRecord.class,
            schemaRegistryUrl);

    final FlinkKafkaConsumer<AggregatedActivePowerRecord> kafkaOutputSource =
        new FlinkKafkaConsumer<>(
            outputTopic, outputSerde, kafkaProps);

    kafkaOutputSource.setStartFromGroupOffsets();
    if (checkpointing) {
      kafkaOutputSource.setCommitOffsetsOnCheckpoints(true);
    }

    // Source from configuration topic with EventSensorRegistry JSON
    final FlinkKafkaKeyValueSerde<Event, String> configSerde =
        new FlinkKafkaKeyValueSerde<>(
            configurationTopic,
            EventSerde::serde,
            Serdes::String,
            TypeInformation.of(new TypeHint<Tuple2<Event, String>>() {}));

    final FlinkKafkaConsumer<Tuple2<Event, String>> kafkaConfigSource = new FlinkKafkaConsumer<>(
        configurationTopic, configSerde, kafkaProps);
    kafkaConfigSource.setStartFromGroupOffsets();
    if (checkpointing) {
      kafkaConfigSource.setCommitOffsetsOnCheckpoints(true);
    }

    // Sink to output topic with SensorId, AggregatedActivePowerRecord
    final FlinkKafkaKeyValueSerde<String, AggregatedActivePowerRecord> aggregationSerde =
        new FlinkKafkaKeyValueSerde<>(
            outputTopic,
            Serdes::String,
            () -> new SchemaRegistryAvroSerdeFactory(schemaRegistryUrl).forValues(),
            TypeInformation.of(new TypeHint<Tuple2<String, AggregatedActivePowerRecord>>() {}));

    final FlinkKafkaProducer<Tuple2<String, AggregatedActivePowerRecord>> kafkaAggregationSink =
        new FlinkKafkaProducer<>(
            outputTopic,
            aggregationSerde,
            kafkaProps,
            FlinkKafkaProducer.Semantic.AT_LEAST_ONCE);
    kafkaAggregationSink.setWriteTimestampToKafka(true);

    // Execution environment configuration
    // org.apache.flink.configuration.Configuration conf = new
    // org.apache.flink.configuration.Configuration();
    // conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true);
    // final StreamExecutionEnvironment env =
    // StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    if (checkpointing) {
      env.enableCheckpointing(commitIntervalMs);
    }

    // State Backend
    if (stateBackend.equals("filesystem")) {
      env.setStateBackend(new FsStateBackend(stateBackendPath));
    } else if (stateBackend.equals("rocksdb")) {
      try {
        env.setStateBackend(new RocksDBStateBackend(stateBackendPath, true));
      } catch (final IOException e) {
        LOGGER.error("Cannot create RocksDB state backend.", e);
      }
    } else {
      env.setStateBackend(new MemoryStateBackend(memoryStateBackendSize));
    }

    // Kryo serializer registration
    env.getConfig().registerTypeWithKryoSerializer(ImmutableSensorRegistry.class,
        new ImmutableSensorRegistrySerializer());
    env.getConfig().registerTypeWithKryoSerializer(SensorParentKey.class,
        new SensorParentKeySerializer());

    env.getConfig().registerTypeWithKryoSerializer(Set.of().getClass(),
        new ImmutableSetSerializer());
    env.getConfig().registerTypeWithKryoSerializer(Set.of(1).getClass(),
        new ImmutableSetSerializer());
    env.getConfig().registerTypeWithKryoSerializer(Set.of(1, 2, 3, 4).getClass(), // NOCS
        new ImmutableSetSerializer());

    env.getConfig().getRegisteredTypesWithKryoSerializers()
        .forEach((c, s) -> LOGGER.info("Class " + c.getName() + " registered with serializer "
            + s.getSerializer().getClass().getName()));

    // Streaming topology

    // Build input stream
    final DataStream<ActivePowerRecord> inputStream = env.addSource(kafkaInputSource)
        .name("[Kafka Consumer] Topic: " + inputTopic)// NOCS
        .rebalance()
        .map(r -> r)
        .name("[Map] Rebalance Forward");

    // Build aggregation stream
    final DataStream<ActivePowerRecord> aggregationsInputStream = env.addSource(kafkaOutputSource)
        .name("[Kafka Consumer] Topic: " + outputTopic) // NOCS
        .rebalance()
        .map(r -> new ActivePowerRecord(r.getIdentifier(), r.getTimestamp(), r.getSumInW()))
        .name("[Map] AggregatedActivePowerRecord -> ActivePowerRecord");

    // Merge input and aggregation streams
    final DataStream<ActivePowerRecord> mergedInputStream = inputStream
        .union(aggregationsInputStream);

    if (debug) {
      mergedInputStream.print();
    }
    // Build parent sensor stream from configuration stream
    final DataStream<Tuple2<String, Set<String>>> configurationsStream =
        env.addSource(kafkaConfigSource)
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

    if (debug) {
      lastValueStream
          .map(t -> "<" + t.f0.getSensor() + "|" + t.f0.getParent() + ">" + t.f1)
          .print();
    }

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

    // add stdout sink
    if (debug) {
      aggregationStream.print();
    }

    // Execution plan
    LOGGER.info("Execution plan: {}", env.getExecutionPlan());

    // Execute Job
    try {
      env.execute(applicationId);
    } catch (final Exception e) { // NOPMD Execution thrown by Flink
      LOGGER.error("An error occured while running this job.", e);
    }
  }

  public static void main(final String[] args) {
    new AggregationServiceFlinkJob().run();
  }
}
