package rocks.theodolite.benchmarks.uc2.flink;

import com.google.common.math.Stats;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.theodolite.benchmarks.commons.flink.AbstractFlinkService;
import rocks.theodolite.benchmarks.commons.flink.KafkaConnectorFactory;
import rocks.theodolite.benchmarks.commons.flink.serialization.StatsSerializer;
import titan.ccp.model.records.ActivePowerRecord;


/**
 * The History microservice implemented as a Flink job.
 */
public final class HistoryServiceFlinkJob extends AbstractFlinkService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryServiceFlinkJob.class);


  /**
   * Create a new instance of the {@link HistoryServiceFlinkJob}.
   */
  public HistoryServiceFlinkJob() {
    super();
  }

  protected void configureSerializers() {
    this.env.getConfig().registerTypeWithKryoSerializer(Stats.class, new StatsSerializer());
    this.env.getConfig().getRegisteredTypesWithKryoSerializers()
        .forEach((c, s) -> LOGGER.info("Class " + c.getName() + " registered with serializer "
            + s.getSerializer().getClass().getName()));

  }


  protected void buildPipeline() {
    final String kafkaBroker = this.config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS);
    final String schemaRegistryUrl = this.config.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL);
    final String inputTopic = this.config.getString(ConfigurationKeys.KAFKA_INPUT_TOPIC);
    final String outputTopic = this.config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC);
    final int windowDurationMinutes =
        this.config.getInt(ConfigurationKeys.KAFKA_WINDOW_DURATION_MINUTES);
    final Time windowDuration = Time.minutes(windowDurationMinutes);
    final boolean checkpointing = this.config.getBoolean(ConfigurationKeys.CHECKPOINTING, true);

    final KafkaConnectorFactory kafkaConnector = new KafkaConnectorFactory(
        this.applicationId, kafkaBroker, checkpointing, schemaRegistryUrl);

    final FlinkKafkaConsumer<ActivePowerRecord> kafkaSource =
        kafkaConnector.createConsumer(inputTopic, ActivePowerRecord.class);

    final FlinkKafkaProducer<Tuple2<String, String>> kafkaSink =
        kafkaConnector.createProducer(outputTopic,
            Serdes::String,
            Serdes::String,
            Types.TUPLE(Types.STRING, Types.STRING));

    this.env
        .addSource(kafkaSource).name("[Kafka Consumer] Topic: " + inputTopic)
        // .rebalance()
        .keyBy(ActivePowerRecord::getIdentifier)
        .window(TumblingEventTimeWindows.of(windowDuration))
        .aggregate(new StatsAggregateFunction(), new StatsProcessWindowFunction())
        .map(t -> {
          final String key = t.f0;
          final String value = t.f1.toString();
          LOGGER.info("{}: {}", key, value);
          return new Tuple2<>(key, value);
        }).name("map").returns(Types.TUPLE(Types.STRING, Types.STRING))
        .addSink(kafkaSink).name("[Kafka Producer] Topic: " + outputTopic);
  }

  public static void main(final String[] args) {
    new HistoryServiceFlinkJob().run();
  }
}
