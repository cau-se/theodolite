package theodolite.uc1.application;

import org.apache.commons.configuration2.Configuration;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theodolite.commons.flink.KafkaConnectorFactory;
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
    final boolean checkpointing = this.config.getBoolean(ConfigurationKeys.CHECKPOINTING, true);
    final int commitIntervalMs = this.config.getInt(ConfigurationKeys.COMMIT_INTERVAL_MS);
    if (checkpointing) {
      this.env.enableCheckpointing(commitIntervalMs);
    }
  }

  private void buildPipeline() {
    final String kafkaBroker = this.config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS);
    final String schemaRegistryUrl = this.config.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL);
    final String inputTopic = this.config.getString(ConfigurationKeys.KAFKA_INPUT_TOPIC);
    final boolean checkpointing = this.config.getBoolean(ConfigurationKeys.CHECKPOINTING, true);

    final KafkaConnectorFactory kafkaConnector = new KafkaConnectorFactory(
        this.applicationId, kafkaBroker, checkpointing, schemaRegistryUrl);

    final FlinkKafkaConsumer<ActivePowerRecord> kafkaConsumer =
        kafkaConnector.createConsumer(inputTopic, ActivePowerRecord.class);

    final DataStream<ActivePowerRecord> stream = this.env.addSource(kafkaConsumer);

    stream
        .rebalance()
        .map(new GsonMapper())
        .flatMap((record, c) -> LOGGER.info("Record: {}", record))
        .returns(Types.GENERIC(Object.class)); // Will never be used
  }

  /**
   * Start running this microservice.
   */
  public void run() {
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
