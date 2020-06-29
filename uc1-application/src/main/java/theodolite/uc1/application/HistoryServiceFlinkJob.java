package theodolite.uc1.application;

import java.util.Properties;
import org.apache.commons.configuration2.Configuration;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import titan.ccp.common.configuration.Configurations;
import titan.ccp.models.records.ActivePowerRecord;

/**
 * The History Microservice Flink Job.
 */
public class HistoryServiceFlinkJob {

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

    final Properties kafkaProps = new Properties();
    kafkaProps.setProperty("bootstrap.servers", kafkaBroker);
    kafkaProps.setProperty("group.id", applicationId);

    final FlinkKafkaConsumer<ActivePowerRecord> kafka = new FlinkKafkaConsumer<>(
        inputTopic, new ActivePowerRecordDeSerializer(inputTopic), kafkaProps);
    kafka.setStartFromGroupOffsets();
    kafka.setCommitOffsetsOnCheckpoints(true);

    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    env.enableCheckpointing(commitIntervalMs);
    env.setParallelism(numThreads);

    final DataStream<ActivePowerRecord> stream = env.addSource(kafka);

    stream
        .keyBy((KeySelector<ActivePowerRecord, String>) ActivePowerRecord::getIdentifier)
        .map(v -> "ActivePowerRecord { "
            + "identifier: " + v.getIdentifier() + ", "
            + "timestamp: " + v.getTimestamp() + ", "
            + "valueInW: " + v.getValueInW() + " }")
        .print();

    try {
      env.execute(applicationId);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(final String[] args) {
    new HistoryServiceFlinkJob().run();
  }
}
