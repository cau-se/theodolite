package theodolite.uc3.application;

import com.google.common.math.Stats;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.apache.commons.configuration2.Configuration;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theodolite.commons.flink.KafkaConnectorFactory;
import theodolite.commons.flink.StateBackends;
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
public final class HistoryServiceFlinkJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryServiceFlinkJob.class);

  private final Configuration config = ServiceConfigurations.createWithDefaults();
  private final StreamExecutionEnvironment env;
  private final String applicationId;

  /**
   * Create a new instance of the {@link HistoryServiceFlinkJob}.
   */
  public HistoryServiceFlinkJob() {
    final String applicationName = this.config.getString(ConfigurationKeys.APPLICATION_NAME);
    final String applicationVersion = this.config.getString(ConfigurationKeys.APPLICATION_VERSION);
    this.applicationId = applicationName + "-" + applicationVersion;

    this.env = StreamExecutionEnvironment.getExecutionEnvironment();

    this.configureEnv();

    this.buildPipeline();
  }

  private void configureEnv() {
    this.env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

    final boolean checkpointing = this.config.getBoolean(ConfigurationKeys.CHECKPOINTING, true);
    final int commitIntervalMs = this.config.getInt(ConfigurationKeys.COMMIT_INTERVAL_MS);
    if (checkpointing) {
      this.env.enableCheckpointing(commitIntervalMs);
    }

    // Parallelism
    final Integer parallelism = this.config.getInteger(ConfigurationKeys.PARALLELISM, null);
    if (parallelism != null) {
      LOGGER.error("Set parallelism: {}.", parallelism);
      this.env.setParallelism(parallelism);
    }

    // State Backend
    final StateBackend stateBackend = StateBackends.fromConfiguration(this.config);
    this.env.setStateBackend(stateBackend);

    this.configureSerializers();
  }

  private void configureSerializers() {
    this.env.getConfig().registerTypeWithKryoSerializer(HourOfDayKey.class,
        new HourOfDayKeySerde());
    this.env.getConfig().registerTypeWithKryoSerializer(Stats.class, new StatsSerializer());
    for (final var entry : this.env.getConfig().getRegisteredTypesWithKryoSerializers()
        .entrySet()) {
      LOGGER.info("Class {} registered with serializer {}.",
          entry.getKey().getName(),
          entry.getValue().getSerializer().getClass().getName());
    }
  }

  private void buildPipeline() {
    // Configurations
    final String kafkaBroker = this.config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS);
    final String schemaRegistryUrl = this.config.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL);
    final String inputTopic = this.config.getString(ConfigurationKeys.KAFKA_INPUT_TOPIC);
    final String outputTopic = this.config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC);
    final ZoneId timeZone = ZoneId.of(this.config.getString(ConfigurationKeys.TIME_ZONE));
    final Time aggregationDuration =
        Time.days(this.config.getInt(ConfigurationKeys.AGGREGATION_DURATION_DAYS));
    final Time aggregationAdvance =
        Time.days(this.config.getInt(ConfigurationKeys.AGGREGATION_ADVANCE_DAYS));
    final boolean checkpointing = this.config.getBoolean(ConfigurationKeys.CHECKPOINTING, true);

    final KafkaConnectorFactory kafkaConnector = new KafkaConnectorFactory(
        this.applicationId, kafkaBroker, checkpointing, schemaRegistryUrl);

    // Sources and Sinks
    final FlinkKafkaConsumer<ActivePowerRecord> kafkaSource =
        kafkaConnector.createConsumer(inputTopic, ActivePowerRecord.class);
    final FlinkKafkaProducer<Tuple2<String, String>> kafkaSink =
        kafkaConnector.createProducer(outputTopic,
            Serdes::String,
            Serdes::String,
            Types.TUPLE(Types.STRING, Types.STRING));

    // Streaming topology
    final StatsKeyFactory<HourOfDayKey> keyFactory = new HourOfDayKeyFactory();
    this.env
        .addSource(kafkaSource).name("[Kafka Consumer] Topic: " + inputTopic)
        // .rebalance()
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
  }

  /**
   * Start running this microservice.
   */
  public void run() {
    // Execution plan
    LOGGER.info("Execution Plan: {}", this.env.getExecutionPlan());

    // Execute Job
    try {
      this.env.execute(this.applicationId);
    } catch (final Exception e) { // NOPMD Execution thrown by Flink
      LOGGER.error("An error occured while running this job.", e);
    }
  }

  public static void main(final String[] args) {
    new HistoryServiceFlinkJob().run();
  }
}
