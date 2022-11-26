package rocks.theodolite.benchmarks.uc2.kstreams;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.streams.KafkaStreams;
import rocks.theodolite.benchmarks.commons.commons.configuration.ServiceConfigurations;
import rocks.theodolite.benchmarks.commons.kstreams.ConfigurationKeys;

/**
 * A microservice that manages the history and, therefore, stores and aggregates incoming
 * measurements.
 *
 */
public class HistoryService {

  private final Configuration config = ServiceConfigurations.createWithDefaults();

  private final CompletableFuture<Void> stopEvent = new CompletableFuture<>();

  /**
   * Start the service.
   */
  public void run() {
    this.createKafkaStreamsApplication();
  }

  /**
   * Build and start the underlying Kafka Streams application of the service.
   *
   */
  private void createKafkaStreamsApplication() {
    final Uc2KafkaStreamsBuilder uc2KafkaStreamsBuilder = new Uc2KafkaStreamsBuilder(this.config);
    uc2KafkaStreamsBuilder
        .outputTopic(this.config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC))
        .windowDuration(Duration.ofMinutes(
            this.config.getInt(ConfigurationKeys.DOWNSAMPLE_INTERVAL_MINUTES)));

    final KafkaStreams kafkaStreams = uc2KafkaStreamsBuilder.build();

    this.stopEvent.thenRun(kafkaStreams::close);
    kafkaStreams.start();
  }

  public static void main(final String[] args) {
    new HistoryService().run();
  }

}
