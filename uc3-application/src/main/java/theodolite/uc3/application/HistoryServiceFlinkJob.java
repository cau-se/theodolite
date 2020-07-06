package theodolite.uc3.application;

import com.google.common.math.Stats;
import java.util.Properties;
import org.apache.commons.configuration2.Configuration;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.common.configuration.Configurations;
import titan.ccp.models.records.ActivePowerRecord;
import titan.ccp.models.records.ActivePowerRecordFactory;


/**
 * The History Microservice Flink Job.
 */
public class HistoryServiceFlinkJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryServiceFlinkJob.class);

  private final Configuration config = Configurations.create();

  private void run() {
    final String applicationName = this.config.getString(ConfigurationKeys.APPLICATION_NAME);
    final String applicationVersion = this.config.getString(ConfigurationKeys.APPLICATION_VERSION);
    final String applicationId = applicationName + "-" + applicationVersion;
    final int numThreads = this.config.getInt(ConfigurationKeys.NUM_THREADS);
    final int commitIntervalMs = this.config.getInt(ConfigurationKeys.COMMIT_INTERVAL_MS);
    //final int maxBytesBuffering = this.config.getInt(ConfigurationKeys.CACHE_MAX_BYTES_BUFFERING);
    final String kafkaBroker = this.config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS);
    final String inputTopic = this.config.getString(ConfigurationKeys.KAFKA_INPUT_TOPIC);
    final String outputTopic = this.config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC);
    final int windowDuration = this.config.getInt(ConfigurationKeys.KAFKA_WINDOW_DURATION_MINUTES);

    final Properties kafkaProps = new Properties();
    kafkaProps.setProperty("bootstrap.servers", kafkaBroker);
    kafkaProps.setProperty("group.id", applicationId);

    final FlinkMonitoringRecordSerde<ActivePowerRecord, ActivePowerRecordFactory> serde =
        new FlinkMonitoringRecordSerde<>(inputTopic,
                                         ActivePowerRecord.class,
                                         ActivePowerRecordFactory.class);

    final FlinkKafkaConsumer<ActivePowerRecord> kafkaSource = new FlinkKafkaConsumer<>(
        inputTopic, serde, kafkaProps);

    kafkaSource.setStartFromGroupOffsets();
    kafkaSource.setCommitOffsetsOnCheckpoints(true);
    kafkaSource.assignTimestampsAndWatermarks(new AscendingTimestampExtractor<>() {
      private static final long serialVersionUID = 908331665581359352L; // NOPMD
      @Override
      public long extractAscendingTimestamp(final ActivePowerRecord element) {
        return element.getTimestamp();
      }
    });

    final FlinkKafkaProducer<String> kafkaSink = new FlinkKafkaProducer<>(
        outputTopic, new SimpleStringSchema(), kafkaProps);
    kafkaSink.setWriteTimestampToKafka(true);

    // environment with Web-GUI for development (included in deployment)
    //org.apache.flink.configuration.Configuration conf =
    //    new org.apache.flink.configuration.Configuration();
    //conf.setInteger("rest.port", 8081);
    //final StreamExecutionEnvironment env =
    //    StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);

    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    env.enableCheckpointing(commitIntervalMs);
    env.setParallelism(numThreads);

    env.getConfig().registerTypeWithKryoSerializer(ActivePowerRecord.class,
        new FlinkMonitoringRecordSerde<>(
            inputTopic,
            ActivePowerRecord.class,
            ActivePowerRecordFactory.class));
    env.getConfig().registerTypeWithKryoSerializer(Stats.class, new StatsSerializer());

    env.getConfig().getRegisteredTypesWithKryoSerializers().forEach((c, s) ->
        LOGGER.info("Class " + c.getName() + " registered with serializer "
            + s.getSerializer().getClass().getName()));

    final DataStream<ActivePowerRecord> stream = env.addSource(kafkaSource)
        .name("[Kafka Consumer] Topic: " + inputTopic);

    stream
        .keyBy((KeySelector<ActivePowerRecord, String>) ActivePowerRecord::getIdentifier)
        .window(TumblingEventTimeWindows.of(Time.minutes(windowDuration)))
        .aggregate(new StatsAggregateFunction())//.name("aggregate")
        .map(Stats::toString).name("map toString")
        .map(record -> {
          LOGGER.info(record);
          return record;
        }).name("map print")
        .addSink(kafkaSink).name("[Kafka Producer] Topic: " + outputTopic);

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Execution Plan: " + env.getExecutionPlan());
    }

    try {
      env.execute(applicationId);
    } catch (Exception e) { //NOPMD
      e.printStackTrace(); //NOPMD
    }
  }

  public static void main(final String[] args) {
    new HistoryServiceFlinkJob().run();
  }
}
