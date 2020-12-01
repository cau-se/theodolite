package theodolite.uc3.application;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.streams.KafkaStreams;
import theodolite.commons.kafkastreams.ConfigurationKeys;
import theodolite.uc3.streamprocessing.Uc3KafkaStreamsBuilder;
import titan.ccp.common.configuration.ServiceConfigurations;

/**
 * A microservice that manages the history and, therefore, stores and aggregates incoming
 * measurements.
 *
 */
public class HistoryService {

  private final Configuration config = ServiceConfigurations.createWithDefaults();

  private final CompletableFuture<Void> stopEvent = new CompletableFuture<>();
  private final int windowDurationMinutes = Integer
      .parseInt(Objects.requireNonNullElse(System.getenv("KAFKA_WINDOW_DURATION_MINUTES"), "60"));

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
    final Uc3KafkaStreamsBuilder uc3KafkaStreamsBuilder = new Uc3KafkaStreamsBuilder(this.config);
    uc3KafkaStreamsBuilder
        .outputTopic(this.config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC))
        .windowDuration(Duration.ofMinutes(this.windowDurationMinutes));

    final KafkaStreams kafkaStreams = uc3KafkaStreamsBuilder.build();

    this.stopEvent.thenRun(kafkaStreams::close);
    kafkaStreams.start();
  }

  public static void main(final String[] args) {
    new HistoryService().run();
  }

}
