package uc1.workloadGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import kafkaSender.KafkaRecordSender;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.models.records.ActivePowerRecord;

public class LoadGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadGenerator.class);

  private static final int WL_MAX_RECORDS = 150_000;

  public static void main(final String[] args) throws InterruptedException, IOException {
    LOGGER.info("Start workload generator for use case UC1.");

    final int numSensors =
        Integer.parseInt(Objects.requireNonNullElse(System.getenv("NUM_SENSORS"), "10"));
    final int instanceId = getInstanceId();
    final int periodMs =
        Integer.parseInt(Objects.requireNonNullElse(System.getenv("PERIOD_MS"), "1000"));
    final int value = Integer.parseInt(Objects.requireNonNullElse(System.getenv("VALUE"), "10"));
    final int threads = Integer.parseInt(Objects.requireNonNullElse(System.getenv("THREADS"), "4"));
    final String kafkaBootstrapServers =
        Objects.requireNonNullElse(System.getenv("KAFKA_BOOTSTRAP_SERVERS"), "localhost:9092");
    final String kafkaInputTopic =
        Objects.requireNonNullElse(System.getenv("KAFKA_INPUT_TOPIC"), "input");
    final String kafkaBatchSize = System.getenv("KAFKA_BATCH_SIZE");
    final String kafkaLingerMs = System.getenv("KAFKA_LINGER_MS");
    final String kafkaBufferMemory = System.getenv("KAFKA_BUFFER_MEMORY");

    final int idStart = instanceId * WL_MAX_RECORDS;
    final int idEnd = Math.min((instanceId + 1) * WL_MAX_RECORDS, numSensors);
    LOGGER.info("Generating data for sensors with IDs from {} to {} (exclusive).", idStart, idEnd);
    final List<String> sensors = IntStream.range(idStart, idEnd)
        .mapToObj(i -> "s_" + i)
        .collect(Collectors.toList());

    final Properties kafkaProperties = new Properties();
    // kafkaProperties.put("acks", this.acknowledges);
    kafkaProperties.compute(ProducerConfig.BATCH_SIZE_CONFIG, (k, v) -> kafkaBatchSize);
    kafkaProperties.compute(ProducerConfig.LINGER_MS_CONFIG, (k, v) -> kafkaLingerMs);
    kafkaProperties.compute(ProducerConfig.BUFFER_MEMORY_CONFIG, (k, v) -> kafkaBufferMemory);
    final KafkaRecordSender<ActivePowerRecord> kafkaRecordSender = new KafkaRecordSender<>(
        kafkaBootstrapServers,
        kafkaInputTopic,
        r -> r.getIdentifier(),
        r -> r.getTimestamp(),
        kafkaProperties);

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(threads);
    final Random random = new Random();

    for (final String sensor : sensors) {
      final int initialDelay = random.nextInt(periodMs);
      executor.scheduleAtFixedRate(() -> {
        kafkaRecordSender.write(new ActivePowerRecord(sensor, System.currentTimeMillis(), value));
      }, initialDelay, periodMs, TimeUnit.MILLISECONDS);
    }

    System.out.println("Wait for termination...");
    executor.awaitTermination(30, TimeUnit.DAYS);
    System.out.println("Will terminate now");

  }

  private static int getInstanceId() {
    final String podName = System.getenv("POD_NAME");
    if (podName == null) {
      return 0;
    } else {
      return Pattern.compile("-")
          .splitAsStream(podName)
          .reduce((p, x) -> x)
          .map(Integer::parseInt)
          .orElse(0);
    }
  }

}
