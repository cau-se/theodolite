package rocks.theodolite.benchmarks.uc3.flink;

import com.google.common.math.Stats;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.ContinuousProcessingTimeTrigger;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.theodolite.benchmarks.commons.flink.AbstractFlinkService;
import rocks.theodolite.benchmarks.commons.flink.KafkaConnectorFactory;
import rocks.theodolite.benchmarks.commons.flink.serialization.StatsSerializer;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;
import rocks.theodolite.benchmarks.uc3.flink.util.HourOfDayKey;
import rocks.theodolite.benchmarks.uc3.flink.util.HourOfDayKeyFactory;
import rocks.theodolite.benchmarks.uc3.flink.util.HourOfDayKeySerde;
import rocks.theodolite.benchmarks.uc3.flink.util.StatsKeyFactory;

/**
 * The History microservice implemented as a Flink job.
 */
public final class HistoryServiceFlinkJob extends AbstractFlinkService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryServiceFlinkJob.class);

  @Override
  protected void configureSerializers() {
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

  @Override
  protected void buildPipeline() {
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
    final Time triggerDuration =
        Time.seconds(this.config.getInt(ConfigurationKeys.AGGREGATION_TRIGGER_INTERVAL_SECONDS));
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
        .trigger(ContinuousProcessingTimeTrigger.of(triggerDuration))
        .aggregate(new StatsAggregateFunction(), new HourOfDayProcessWindowFunction())
        .map(tuple -> {
          final String sensorId = keyFactory.getSensorId(tuple.f0);
          final String stats = tuple.f1.toString();
          // final int hourOfDay = tuple.f0.getHourOfDay();
          // LOGGER.info("{}|{}: {}", newKey, hourOfDay, newValue);
          return new Tuple2<>(sensorId, stats);
        })
        .name("map")
        .returns(Types.TUPLE(Types.STRING, Types.STRING))
        .addSink(kafkaSink).name("[Kafka Producer] Topic: " + outputTopic);
  }

  public static void main(final String[] args) {
    new HistoryServiceFlinkJob().run();
  }
}
