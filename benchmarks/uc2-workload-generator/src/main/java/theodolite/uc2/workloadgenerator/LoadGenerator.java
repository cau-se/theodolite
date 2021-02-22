package theodolite.uc2.workloadgenerator;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theodolite.commons.workloadgeneration.KeySpace;
import theodolite.commons.workloadgeneration.communication.kafka.KafkaRecordSender;
import theodolite.commons.workloadgeneration.generators.KafkaWorkloadGenerator;
import theodolite.commons.workloadgeneration.generators.KafkaWorkloadGeneratorBuilder;
import theodolite.commons.workloadgeneration.misc.ZooKeeper;
import titan.ccp.configuration.events.Event;
import titan.ccp.model.records.ActivePowerRecord;
import titan.ccp.model.sensorregistry.SensorRegistry;

/**
 * The {@code LoadGenerator} creates a load in Kafka.
 */
public final class LoadGenerator {

  private static final int SLEEP_PERIOD = 30_000;

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadGenerator.class);

  // Constants
  private static final String DEEP = "deep";
  private static final long MAX_DURATION_IN_DAYS = 30L;

  // Make this a utility class, because all methods are static.
  private LoadGenerator() {
    throw new UnsupportedOperationException();
  }

  /**
   * Main method.
   *
   * @param args CLI arguments
   * @throws InterruptedException Interrupt happened
   * @throws IOException happened.
   */
  public static void main(final String[] args) throws InterruptedException, IOException {
    // uc2
    LOGGER.info("Start workload generator for use case UC2.");

    // get environment variables
    final String hierarchy = System.getenv("HIERARCHY");
    if (hierarchy != null && hierarchy.equals(DEEP)) {
      LOGGER.error(
          "The HIERARCHY parameter is no longer supported. Creating a full hierachy instead.");
    }
    final int numNestedGroups = Integer
        .parseInt(Objects.requireNonNullElse(System.getenv("NUM_NESTED_GROUPS"), "1"));
    final String zooKeeperHost = Objects.requireNonNullElse(System.getenv("ZK_HOST"), "localhost");
    final int zooKeeperPort =
        Integer.parseInt(Objects.requireNonNullElse(System.getenv("ZK_PORT"), "2181"));
    final int numSensors =
        Integer.parseInt(Objects.requireNonNullElse(System.getenv("NUM_SENSORS"), "1"));
    final int periodMs =
        Integer.parseInt(Objects.requireNonNullElse(System.getenv("PERIOD_MS"), "1000"));
    final double value =
        Double.parseDouble(Objects.requireNonNullElse(System.getenv("VALUE"), "10"));
    final boolean sendRegistry = Boolean
        .parseBoolean(Objects.requireNonNullElse(System.getenv("SEND_REGISTRY"), "true"));
    final int threads = Integer.parseInt(Objects.requireNonNullElse(System.getenv("THREADS"), "4"));
    final String kafkaBootstrapServers =
        Objects.requireNonNullElse(System.getenv("KAFKA_BOOTSTRAP_SERVERS"),
            "localhost:9092");
    final String schemaRegistryUrl =
        Objects.requireNonNullElse(System.getenv("SCHEMA_REGISTRY_URL"), "http://localhost:8091");
    final String kafkaInputTopic =
        Objects.requireNonNullElse(System.getenv("KAFKA_INPUT_TOPIC"), "input");
    final String kafkaBatchSize = System.getenv("KAFKA_BATCH_SIZE");
    final String kafkaLingerMs = System.getenv("KAFKA_LINGER_MS");
    final String kafkaBufferMemory = System.getenv("KAFKA_BUFFER_MEMORY");
    final int instances =
        Integer.parseInt(Objects.requireNonNullElse(System.getenv("INSTANCES"), "1"));

    // build sensor registry
    final SensorRegistry sensorRegistry =
        new SensorRegistryBuilder(numNestedGroups, numSensors).build();

    // create kafka record sender
    final Properties kafkaProperties = new Properties();
    // kafkaProperties.put("acks", this.acknowledges);
    kafkaProperties.compute(ProducerConfig.BATCH_SIZE_CONFIG, (k, v) -> kafkaBatchSize);
    kafkaProperties.compute(ProducerConfig.LINGER_MS_CONFIG, (k, v) -> kafkaLingerMs);
    kafkaProperties.compute(ProducerConfig.BUFFER_MEMORY_CONFIG, (k, v) -> kafkaBufferMemory);

    final KafkaRecordSender<ActivePowerRecord> kafkaRecordSender =
        new KafkaRecordSender.Builder<ActivePowerRecord>(
            kafkaBootstrapServers,
            kafkaInputTopic,
            schemaRegistryUrl)
                .keyAccessor(r -> r.getIdentifier())
                .timestampAccessor(r -> r.getTimestamp())
                .defaultProperties(kafkaProperties)
                .build();

    // create workload generator
    final KafkaWorkloadGenerator<ActivePowerRecord> workloadGenerator =
        KafkaWorkloadGeneratorBuilder.<ActivePowerRecord>builder()
            .instances(instances)
            .keySpace(new KeySpace("s_", sensorRegistry.getMachineSensors().size()))
            .threads(threads)
            .period(Duration.of(periodMs, ChronoUnit.MILLIS))
            .duration(Duration.of(MAX_DURATION_IN_DAYS, ChronoUnit.DAYS))
            .beforeAction(() -> {
              if (sendRegistry) {
                final ConfigPublisher configPublisher =
                    new ConfigPublisher(kafkaBootstrapServers, "configuration");
                configPublisher.publish(Event.SENSOR_REGISTRY_CHANGED, sensorRegistry.toJson());
                configPublisher.close();
                LOGGER.info("Configuration sent.");

                LOGGER.info("Now wait 30 seconds");
                try {
                  Thread.sleep(SLEEP_PERIOD);
                } catch (final InterruptedException e) {
                  // TODO Auto-generated catch block
                  LOGGER.error(e.getMessage(), e);
                }
                LOGGER.info("And woke up again :)");
              }
            })
            .generatorFunction(
                sensor -> new ActivePowerRecord(sensor, System.currentTimeMillis(), value))
            .zooKeeper(new ZooKeeper(zooKeeperHost, zooKeeperPort))
            .kafkaRecordSender(kafkaRecordSender)
            .build();

    // start
    workloadGenerator.start();
  }

}
