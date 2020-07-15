package theodolite.uc4.application;

import com.google.common.math.Stats;
import org.apache.commons.configuration2.Configuration;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theodolite.commons.flink.serialization.FlinkKafkaKeyValueSerde;
import theodolite.commons.flink.serialization.FlinkMonitoringRecordSerde;
import theodolite.commons.flink.serialization.StatsSerializer;
import theodolite.uc4.application.util.*;
import titan.ccp.common.configuration.Configurations;
import titan.ccp.models.records.ActivePowerRecord;
import titan.ccp.models.records.ActivePowerRecordFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Properties;


/**
 * The History Microservice Flink Job.
 */
public class HistoryServiceFlinkJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryServiceFlinkJob.class);

  private final Configuration config = Configurations.create();

  private void run() {
    // Configurations
    final String applicationName = this.config.getString(ConfigurationKeys.APPLICATION_NAME);
    final String applicationVersion = this.config.getString(ConfigurationKeys.APPLICATION_VERSION);
    final String applicationId = applicationName + "-" + applicationVersion;
    final int numThreads = this.config.getInt(ConfigurationKeys.NUM_THREADS);
    final int commitIntervalMs = this.config.getInt(ConfigurationKeys.COMMIT_INTERVAL_MS);
    //final int maxBytesBuffering = this.config.getInt(ConfigurationKeys.CACHE_MAX_BYTES_BUFFERING);
    final String kafkaBroker = this.config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS);
    final String inputTopic = this.config.getString(ConfigurationKeys.KAFKA_INPUT_TOPIC);
    final String outputTopic = this.config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC);
    final String timeZoneString = this.config.getString(ConfigurationKeys.TIME_ZONE);
    final ZoneId timeZone = ZoneId.of(timeZoneString);
    final Time aggregationDuration =
        Time.days(this.config.getInt(ConfigurationKeys.AGGREGATION_DURATION_DAYS));
    final Time aggregationAdvance =
        Time.days(this.config.getInt(ConfigurationKeys.AGGREGATION_ADVANCE_DAYS));

    final Properties kafkaProps = new Properties();
    kafkaProps.setProperty("bootstrap.servers", kafkaBroker);
    kafkaProps.setProperty("group.id", applicationId);

    // Sources and Sinks with Serializer and Deserializer

//    final FlinkKafkaKeyValueSerde<String, ActivePowerRecord> sourceSerde =
//        new FlinkKafkaKeyValueSerde<>(inputTopic,
//            Serdes::String,
//            () -> IMonitoringRecordSerde.serde(new ActivePowerRecordFactory()),
//            TypeInformation.of(new TypeHint<Tuple2<String, ActivePowerRecord>>() {})
//        );
    final FlinkMonitoringRecordSerde<ActivePowerRecord, ActivePowerRecordFactory> sourceSerde =
        new FlinkMonitoringRecordSerde<>(
            inputTopic,
            ActivePowerRecord.class,
            ActivePowerRecordFactory.class);

    final FlinkKafkaConsumer<ActivePowerRecord> kafkaSource = new FlinkKafkaConsumer<>(
        inputTopic, sourceSerde, kafkaProps);

    kafkaSource.setStartFromGroupOffsets();
    kafkaSource.setCommitOffsetsOnCheckpoints(true);
    kafkaSource.assignTimestampsAndWatermarks(new AscendingTimestampExtractor<>() {
      private static final long serialVersionUID = 2360564218340313768L; //NOPMD
      @Override
      public long extractAscendingTimestamp(final ActivePowerRecord element) {
        return element.getTimestamp();
      }
    });

    final FlinkKafkaKeyValueSerde<String, String> sinkSerde =
        new FlinkKafkaKeyValueSerde<>(outputTopic,
            Serdes::String,
            Serdes::String,
            TypeInformation.of(new TypeHint<Tuple2<String, String>>() {})
        );

    final FlinkKafkaProducer<Tuple2<String, String>> kafkaSink = new FlinkKafkaProducer<>(
        outputTopic, sinkSerde, kafkaProps, FlinkKafkaProducer.Semantic.AT_LEAST_ONCE);
    kafkaSink.setWriteTimestampToKafka(true);

    // environment with Web-GUI for development (included in deployment)
    //org.apache.flink.configuration.Configuration conf =
    //   new org.apache.flink.configuration.Configuration();
    //conf.setInteger("rest.port", 8081);
    //final StreamExecutionEnvironment env =
    //StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);

    // Execution environment configuration

    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    env.enableCheckpointing(commitIntervalMs);
    env.setParallelism(numThreads);

// TODO: Change StateBackend if necessary (State too big)

//    try {
//      RocksDBStateBackend backend =
//          new RocksDBStateBackend("file:///home/nico/flink-fs-backend", true);
//      env.setStateBackend(backend);
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//    env.setStateBackend(new MemoryStateBackend(Integer.MAX_VALUE));
//    env.setStateBackend(new FsStateBackend("file:///home/nico/flink-fs-backend"));

    // Kryo serializer registration
    env.getConfig().registerTypeWithKryoSerializer(HourOfDayKey.class, new HourOfDayKeySerde());
    env.getConfig().registerTypeWithKryoSerializer(ActivePowerRecord.class,
        new FlinkMonitoringRecordSerde<>(
            inputTopic,
            ActivePowerRecord.class,
            ActivePowerRecordFactory.class));
    env.getConfig().registerTypeWithKryoSerializer(Stats.class, new StatsSerializer());

    env.getConfig().getRegisteredTypesWithKryoSerializers().forEach((c, s) ->
        LOGGER.info("Class " + c.getName() + " registered with serializer "
            + s.getSerializer().getClass().getName()));

    // Streaming topology

    final StatsKeyFactory<HourOfDayKey> keyFactory = new HourOfDayKeyFactory();

    final DataStream<ActivePowerRecord> stream = env.addSource(kafkaSource)
        .name("[Kafka Consumer] Topic: " + inputTopic);

    stream
        .rebalance()
        .keyBy((KeySelector<ActivePowerRecord, HourOfDayKey>) record -> {
          final Instant instant = Instant.ofEpochMilli(record.getTimestamp());
          final LocalDateTime dateTime = LocalDateTime.ofInstant(instant, timeZone);
          return keyFactory.createKey(record.getIdentifier(), dateTime);
        })
        .window(SlidingEventTimeWindows.of(aggregationDuration, aggregationAdvance))
        .aggregate(new StatsAggregateFunction(), new HourOfDayProcessWindowFunction())
        .map(new MapFunction<Tuple2<HourOfDayKey, Stats>, Tuple2<String, String>>() {
          @Override
          public Tuple2<String, String> map(Tuple2<HourOfDayKey, Stats> tuple) {
            final String newKey = keyFactory.getSensorId(tuple.f0);
            final String newValue = tuple.f1.toString();
            final int hourOfDay = tuple.f0.getHourOfDay();
            LOGGER.info(newKey + "|" + hourOfDay + ": " + newValue);
            return new Tuple2<>(newKey, newValue);
          }
        }).name("map")
        .addSink(kafkaSink).name("[Kafka Producer] Topic: " + outputTopic);

    // Execution plan

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Execution Plan: " + env.getExecutionPlan());
    }

    // Execute Job

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
