package theodolite.uc3.application;

import com.google.common.math.Stats;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Properties;
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
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theodolite.commons.flink.serialization.FlinkKafkaKeyValueSerde;
import theodolite.commons.flink.serialization.StatsSerializer;
import theodolite.uc3.application.util.HourOfDayKey;
import theodolite.uc3.application.util.HourOfDayKeyFactory;
import theodolite.uc3.application.util.HourOfDayKeySerde;
import theodolite.uc3.application.util.StatsKeyFactory;
import titan.ccp.common.configuration.ServiceConfigurations;
import titan.ccp.model.records.ActivePowerRecord;


/**
 * The History microservice implemented as a Flink job.
 */
public class HistoryServiceFlinkJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryServiceFlinkJob.class);

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
    final String timeZoneString = this.config.getString(ConfigurationKeys.TIME_ZONE);
    final ZoneId timeZone = ZoneId.of(timeZoneString);
    final Time aggregationDuration =
        Time.days(this.config.getInt(ConfigurationKeys.AGGREGATION_DURATION_DAYS));
    final Time aggregationAdvance =
        Time.days(this.config.getInt(ConfigurationKeys.AGGREGATION_ADVANCE_DAYS));
    final String stateBackend =
        this.config.getString(ConfigurationKeys.FLINK_STATE_BACKEND, "").toLowerCase();
    final String stateBackendPath = this.config
        .getString(ConfigurationKeys.FLINK_STATE_BACKEND_PATH, "/opt/flink/statebackend");
    final int memoryStateBackendSize =
        this.config.getInt(ConfigurationKeys.FLINK_STATE_BACKEND_MEMORY_SIZE,
            MemoryStateBackend.DEFAULT_MAX_STATE_SIZE);
    final boolean checkpointing = this.config.getBoolean(ConfigurationKeys.CHECKPOINTING, true);

    final Properties kafkaProps = new Properties();
    kafkaProps.setProperty("bootstrap.servers", kafkaBroker);
    kafkaProps.setProperty("group.id", applicationId);

    // Sources and Sinks with Serializer and Deserializer

    final DeserializationSchema<ActivePowerRecord> sourceSerde =
        ConfluentRegistryAvroDeserializationSchema.forSpecific(
            ActivePowerRecord.class,
            schemaRegistryUrl);

    final FlinkKafkaConsumer<ActivePowerRecord> kafkaSource = new FlinkKafkaConsumer<>(
        inputTopic, sourceSerde, kafkaProps);

    kafkaSource.setStartFromGroupOffsets();
    if (checkpointing) {
      kafkaSource.setCommitOffsetsOnCheckpoints(true);
    }
    kafkaSource.assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps());

    final FlinkKafkaKeyValueSerde<String, String> sinkSerde =
        new FlinkKafkaKeyValueSerde<>(outputTopic,
            Serdes::String,
            Serdes::String,
            TypeInformation.of(new TypeHint<Tuple2<String, String>>() {}));

    final FlinkKafkaProducer<Tuple2<String, String>> kafkaSink = new FlinkKafkaProducer<>(
        outputTopic, sinkSerde, kafkaProps, FlinkKafkaProducer.Semantic.AT_LEAST_ONCE);
    kafkaSink.setWriteTimestampToKafka(true);

    // Execution environment configuration
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
    env.getConfig().registerTypeWithKryoSerializer(HourOfDayKey.class, new HourOfDayKeySerde());
    env.getConfig().registerTypeWithKryoSerializer(Stats.class, new StatsSerializer());
    for (final var entry : env.getConfig().getRegisteredTypesWithKryoSerializers().entrySet()) {
      LOGGER.info("Class {} registered with serializer {}.",
          entry.getKey().getName(),
          entry.getValue().getSerializer().getClass().getName());
    }

    // Streaming topology
    final StatsKeyFactory<HourOfDayKey> keyFactory = new HourOfDayKeyFactory();
    env
        .addSource(kafkaSource)
        .name("[Kafka Consumer] Topic: " + inputTopic)
        .rebalance()
        .keyBy((KeySelector<ActivePowerRecord, HourOfDayKey>) record -> {
          final Instant instant = Instant.ofEpochMilli(record.getTimestamp());
          final LocalDateTime dateTime = LocalDateTime.ofInstant(instant, timeZone);
          return keyFactory.createKey(record.getIdentifier(), dateTime);
        })
        .window(SlidingEventTimeWindows.of(aggregationDuration, aggregationAdvance))
        .aggregate(new StatsAggregateFunction(), new HourOfDayProcessWindowFunction())
        .map(tuple -> {
          final String newKey = keyFactory.getSensorId(tuple.f0);
          final String newValue = tuple.f1.toString();
          final int hourOfDay = tuple.f0.getHourOfDay();
          LOGGER.info("{}|{}: {}", newKey, hourOfDay, newValue);
          return new Tuple2<>(newKey, newValue);
        })
        .name("map")
        .returns(Types.TUPLE(Types.STRING, Types.STRING))
        .addSink(kafkaSink).name("[Kafka Producer] Topic: " + outputTopic);

    // Execution plan
    LOGGER.info("Execution Plan: {}", env.getExecutionPlan());

    // Execute Job
    try {
      env.execute(applicationId);
    } catch (final Exception e) { // NOPMD Execution thrown by Flink
      LOGGER.error("An error occured while running this job.", e);
    }
  }

  public static void main(final String[] args) {
    new HistoryServiceFlinkJob().run();
  }
}
