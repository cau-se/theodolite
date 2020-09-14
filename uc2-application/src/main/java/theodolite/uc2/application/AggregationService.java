package theodolite.uc2.application;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.streams.KafkaStreams;
import theodolite.commons.kafkastreams.ConfigurationKeys;
import theodolite.uc2.streamprocessing.Uc2KafkaStreamsBuilder;
import titan.ccp.common.configuration.ServiceConfigurations;

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
    // Use case specific stream configuration
    final Uc2KafkaStreamsBuilder uc2KafkaStreamsBuilder = new Uc2KafkaStreamsBuilder();
    uc2KafkaStreamsBuilder
        .inputTopic(this.config.getString(ConfigurationKeys.KAFKA_INPUT_TOPIC))
        .feedbackTopic(this.config.getString(ConfigurationKeys.KAFKA_FEEDBACK_TOPIC))
        .outputTopic(this.config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC))
        .configurationTopic(this.config.getString(ConfigurationKeys.KAFKA_CONFIGURATION_TOPIC))
        .emitPeriod(Duration.ofMillis(this.config.getLong(ConfigurationKeys.EMIT_PERIOD_MS)))
        .gracePeriod(Duration.ofMillis(this.config.getLong(ConfigurationKeys.GRACE_PERIOD_MS)));

    // Configuration of the stream application
    final KafkaStreams kafkaStreams = uc2KafkaStreamsBuilder
        .applicationName(this.config.getString(ConfigurationKeys.APPLICATION_NAME))
        .applicationVersion(this.config.getString(ConfigurationKeys.APPLICATION_VERSION))
        .bootstrapServers(this.config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS))
        .schemaRegistry(this.config.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL))
        .numThreads(this.config.getInt(ConfigurationKeys.NUM_THREADS))
        .commitIntervalMs(this.config.getInt(ConfigurationKeys.COMMIT_INTERVAL_MS))
        .cacheMaxBytesBuffering(this.config.getInt(ConfigurationKeys.CACHE_MAX_BYTES_BUFFERING))
        .build();

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
