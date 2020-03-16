package titan.ccp.kiekerbridge.expbigdata19;

import com.google.common.math.StatsAccumulator;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;
import titan.ccp.common.kieker.kafka.IMonitoringRecordSerde;
import titan.ccp.models.records.AggregatedActivePowerRecord;
import titan.ccp.models.records.AggregatedActivePowerRecordFactory;

public class LoadCounter {

  public static void main(final String[] args) throws InterruptedException {

    final String kafkaBootstrapServers =
        Objects.requireNonNullElse(System.getenv("KAFKA_BOOTSTRAP_SERVERS"), "localhost:9092");
    final String kafkaInputTopic =
        Objects.requireNonNullElse(System.getenv("KAFKA_INPUT_TOPIC"), "input");
    final String kafkaOutputTopic =
        Objects.requireNonNullElse(System.getenv("KAFKA_OUTPUT_TOPIC"), "output");

    final Properties props = new Properties();
    props.setProperty("bootstrap.servers", kafkaBootstrapServers);
    props.setProperty("group.id", "load-counter");
    props.setProperty("enable.auto.commit", "true");
    props.setProperty("auto.commit.interval.ms", "1000");
    props.setProperty("max.poll.records", "1000000");
    props.setProperty("max.partition.fetch.bytes", "134217728"); // 128 MB
    props.setProperty("key.deserializer",
        "org.apache.kafka.common.serialization.StringDeserializer");
    props.setProperty("value.deserializer",
        "org.apache.kafka.common.serialization.ByteArrayDeserializer");

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    final Deserializer<AggregatedActivePowerRecord> deserializer =
        IMonitoringRecordSerde.deserializer(new AggregatedActivePowerRecordFactory());

    final KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(List.of(kafkaInputTopic, kafkaOutputTopic));

    executor.scheduleAtFixedRate(
        () -> {
          final long time = System.currentTimeMillis();
          final ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));

          long inputCount = 0;
          for (final ConsumerRecord<String, byte[]> inputRecord : records
              .records(kafkaInputTopic)) {
            inputCount++;
          }

          long outputCount = 0;
          final StatsAccumulator statsAccumulator = new StatsAccumulator();
          for (final ConsumerRecord<String, byte[]> outputRecord : records
              .records(kafkaOutputTopic)) {
            outputCount++;
            final AggregatedActivePowerRecord record =
                deserializer.deserialize(kafkaOutputTopic, outputRecord.value());
            final long latency = time - record.getTimestamp();
            statsAccumulator.add(latency);
          }

          final double latency = statsAccumulator.count() > 0 ? statsAccumulator.mean() : 0.0;

          final long elapsedTime = System.currentTimeMillis() - time;
          System.out
              .println("input," + time + ',' + elapsedTime + ',' + 0 + ',' + inputCount);
          System.out
              .println("output," + time + ',' + elapsedTime + ',' + latency + ',' + outputCount);
        },
        0,
        1,
        TimeUnit.SECONDS);
  }

}
