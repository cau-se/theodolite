package rocks.theodolite.benchmarks.uc1.flink;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.theodolite.benchmarks.commons.flink.AbstractFlinkService;
import rocks.theodolite.benchmarks.commons.flink.KafkaConnectorFactory;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseAdapter;
import rocks.theodolite.benchmarks.uc1.commons.logger.LogWriterFactory;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * The History microservice implemented as a Flink job.
 */
public final class HistoryServiceFlinkJob extends AbstractFlinkService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryServiceFlinkJob.class);
  private static final DatabaseAdapter<String> databaseAdapter = LogWriterFactory.forJson();

  /**
   * Create a new instance of the {@link HistoryServiceFlinkJob}.
   */
  public HistoryServiceFlinkJob() {
    super();
  }

  public void configureEnv() {
    super.configureCheckpointing();
    super.configureParallelism();
  }

  protected void configureSerializers() {
  }

  protected void buildPipeline() {
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
        // .rebalance()
        .map(new ConverterAdapter<>(this.databaseAdapter.getRecordConverter()))
        .returns(Types.STRING)
        .flatMap(new WriterAdapter<>(this.databaseAdapter.getDatabaseWriter()))
        .returns(Types.VOID); // Will never be used
  }

  public static void main(final String[] args) {
    new HistoryServiceFlinkJob().run();
  }
}
