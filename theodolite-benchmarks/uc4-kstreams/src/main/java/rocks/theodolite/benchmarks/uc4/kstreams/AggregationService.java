package rocks.theodolite.benchmarks.uc4.kstreams;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.streams.KafkaStreams;
import rocks.theodolite.benchmarks.commons.commons.configuration.ServiceConfigurations;

/**
 * A microservice that manages the history and, therefore, stores and aggregates incoming
 * measurements.
 *
 */
public class AggregationService {

  private final Configuration config = ServiceConfigurations.createWithDefaults();

  private final CompletableFuture<Void> stopEvent = new CompletableFuture<>();

  /**
   * Start the service.
   */
  public void run() {
    this.createKafkaStreamsApplication();
  }

  public static void main(final String[] args) {
    new AggregationService().run();
  }

  /**
   * Build and start the underlying Kafka Streams Application of the service.
   *
   * @param clusterSession the database session which the application should use.
   */
  private void createKafkaStreamsApplication() {
    final Uc4KafkaStreamsBuilder uc4KafkaStreamsBuilder = new Uc4KafkaStreamsBuilder(this.config);
    uc4KafkaStreamsBuilder
        // .feedbackTopic(this.config.getString(Uc4ConfigurationKeys.KAFKA_FEEDBACK_TOPIC))
        .outputTopic(this.config.getString(Uc4ConfigurationKeys.KAFKA_OUTPUT_TOPIC))
        .configurationTopic(this.config.getString(Uc4ConfigurationKeys.KAFKA_CONFIGURATION_TOPIC))
        .emitPeriod(Duration.ofMillis(this.config.getLong(Uc4ConfigurationKeys.EMIT_PERIOD_MS)))
        .gracePeriod(Duration.ofMillis(this.config.getLong(Uc4ConfigurationKeys.GRACE_PERIOD_MS)));

    final KafkaStreams kafkaStreams = uc4KafkaStreamsBuilder.build();

    this.stopEvent.thenRun(kafkaStreams::close);
    kafkaStreams.start();
  }

  /**
   * Stop the service.
   */
  public void stop() {
    this.stopEvent.complete(null);
  }

}
