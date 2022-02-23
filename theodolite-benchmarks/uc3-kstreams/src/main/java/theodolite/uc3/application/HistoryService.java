package theodolite.uc3.application;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.streams.KafkaStreams;
import rocks.theodolite.benchmarks.commons.kstreams.ConfigurationKeys;
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
    // Use case specific stream configuration
    final Uc3KafkaStreamsBuilder uc3KafkaStreamsBuilder = new Uc3KafkaStreamsBuilder(this.config);
    uc3KafkaStreamsBuilder
        .outputTopic(this.config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC))
        .aggregtionDuration(
            Duration.ofDays(this.config.getInt(ConfigurationKeys.AGGREGATION_DURATION_DAYS)))
        .aggregationAdvance(
            Duration.ofDays(this.config.getInt(ConfigurationKeys.AGGREGATION_ADVANCE_DAYS)));

    // Configuration of the stream application
    final KafkaStreams kafkaStreams = uc3KafkaStreamsBuilder.build();

    this.stopEvent.thenRun(kafkaStreams::close);
    kafkaStreams.start();
  }

  public static void main(final String[] args) {
    new HistoryService().run();
  }

}
