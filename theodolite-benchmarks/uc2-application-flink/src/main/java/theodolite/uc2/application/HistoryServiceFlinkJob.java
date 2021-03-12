package theodolite.uc2.application;

import com.google.common.math.Stats;
import org.apache.commons.configuration2.Configuration;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theodolite.commons.flink.KafkaConnectorFactory;
import theodolite.commons.flink.StateBackends;
import theodolite.commons.flink.serialization.StatsSerializer;
import titan.ccp.common.configuration.ServiceConfigurations;
import titan.ccp.model.records.ActivePowerRecord;


/**
 * The History microservice implemented as a Flink job.
 */
public class HistoryServiceFlinkJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryServiceFlinkJob.class);

  private final Configuration config = ServiceConfigurations.createWithDefaults();

  private void run() {
    final String applicationName = this.config.getString(ConfigurationKeys.APPLICATION_NAME);
    final String applicationVersion = this.config.getString(ConfigurationKeys.APPLICATION_VERSION);
    final String applicationId = applicationName + "-" + applicationVersion;
    final int commitIntervalMs = this.config.getInt(ConfigurationKeys.COMMIT_INTERVAL_MS);
    final String kafkaBroker = this.config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS);
    final String inputTopic = this.config.getString(ConfigurationKeys.KAFKA_INPUT_TOPIC);
    final String outputTopic = this.config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC);
    final String schemaRegistryUrl = this.config.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL);
    final int windowDuration = this.config.getInt(ConfigurationKeys.KAFKA_WINDOW_DURATION_MINUTES);
    final boolean checkpointing = this.config.getBoolean(ConfigurationKeys.CHECKPOINTING, true);
    final StateBackend stateBackend = StateBackends.fromConfiguration(this.config);

    final KafkaConnectorFactory kafkaConnector = new KafkaConnectorFactory(
        applicationId, kafkaBroker, checkpointing, schemaRegistryUrl);

    final FlinkKafkaConsumer<ActivePowerRecord> kafkaSource =
        kafkaConnector.createConsumer(inputTopic, ActivePowerRecord.class);

    final FlinkKafkaProducer<Tuple2<String, String>> kafkaSink =
        kafkaConnector.createProducer(outputTopic,
            Serdes::String,
            Serdes::String,
            Types.TUPLE(Types.STRING, Types.STRING));

    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    if (checkpointing) {
      env.enableCheckpointing(commitIntervalMs);
    }

    // State Backend
    env.setStateBackend(stateBackend);

    env.getConfig().registerTypeWithKryoSerializer(Stats.class, new StatsSerializer());
    env.getConfig().getRegisteredTypesWithKryoSerializers()
        .forEach((c, s) -> LOGGER.info("Class " + c.getName() + " registered with serializer "
            + s.getSerializer().getClass().getName()));

    env
        .addSource(kafkaSource).name("[Kafka Consumer] Topic: " + inputTopic)
        .rebalance()
        .keyBy(ActivePowerRecord::getIdentifier)
        .window(TumblingEventTimeWindows.of(Time.minutes(windowDuration)))
        .aggregate(new StatsAggregateFunction(), new StatsProcessWindowFunction())
        .map(t -> {
          final String key = t.f0;
          final String value = t.f1.toString();
          LOGGER.info("{}: {}", key, value);
          return new Tuple2<>(key, value);
        }).name("map").returns(Types.TUPLE(Types.STRING, Types.STRING))
        .addSink(kafkaSink).name("[Kafka Producer] Topic: " + outputTopic);

    LOGGER.info("Execution plan: {}", env.getExecutionPlan());

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
