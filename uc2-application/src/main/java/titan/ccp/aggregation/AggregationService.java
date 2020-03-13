package titan.ccp.aggregation;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.streams.KafkaStreams;
import titan.ccp.aggregation.streamprocessing.KafkaStreamsBuilder;
import titan.ccp.common.configuration.Configurations;

/**
 * A microservice that manages the history and, therefore, stores and aggregates incoming
 * measurements.
 *
 */
public class AggregationService {

  private final Configuration config = Configurations.create();

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
    final KafkaStreams kafkaStreams = new KafkaStreamsBuilder()
        .bootstrapServers(this.config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS))
        .inputTopic(this.config.getString(ConfigurationKeys.KAFKA_INPUT_TOPIC))
        .outputTopic(this.config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC))
        .configurationTopic(this.config.getString(ConfigurationKeys.CONFIGURATION_KAFKA_TOPIC))
        .windowSize(Duration.ofMillis(this.config.getLong(ConfigurationKeys.WINDOW_SIZE_MS)))
        .gracePeriod(Duration.ofMillis(this.config.getLong(ConfigurationKeys.WINDOW_GRACE_MS)))
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
