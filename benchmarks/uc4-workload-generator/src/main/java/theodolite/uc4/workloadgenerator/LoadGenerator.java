package theodolite.uc4.workloadgenerator;

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
import titan.ccp.model.records.ActivePowerRecord;

/**
 * The {@code LoadGenerator} creates a load in Kafka.
 */
public final class LoadGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadGenerator.class);

  // constants
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
    // uc4
    LOGGER.info("Start workload generator for use case UC4.");

    // get environment variables
    final String zooKeeperHost = Objects.requireNonNullElse(System.getenv("ZK_HOST"), "localhost");
    final int zooKeeperPort =
        Integer.parseInt(Objects.requireNonNullElse(System.getenv("ZK_PORT"), "2181"));
    final int numSensors =
        Integer.parseInt(Objects.requireNonNullElse(System.getenv("NUM_SENSORS"), "10"));
    final int periodMs =
        Integer.parseInt(Objects.requireNonNullElse(System.getenv("PERIOD_MS"), "1000"));
    final double value =
        Double.parseDouble(Objects.requireNonNullElse(System.getenv("VALUE"), "10"));
    final int threads = Integer.parseInt(Objects.requireNonNullElse(System.getenv("THREADS"), "1"));
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
            .keySpace(new KeySpace("s_", numSensors))
            .threads(threads)
            .period(Duration.of(periodMs, ChronoUnit.MILLIS))
            .duration(Duration.of(MAX_DURATION_IN_DAYS, ChronoUnit.DAYS))
            .generatorFunction(
                sensor -> new ActivePowerRecord(sensor, System.currentTimeMillis(), value))
            .zooKeeper(new ZooKeeper(zooKeeperHost, zooKeeperPort))
            .kafkaRecordSender(kafkaRecordSender)
            .build();

    // start
    workloadGenerator.start();
  }

}
