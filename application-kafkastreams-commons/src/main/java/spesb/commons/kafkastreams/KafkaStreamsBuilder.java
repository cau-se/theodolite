package spesb.commons.kafkastreams;

import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import titan.ccp.common.kafka.streams.PropertiesBuilder;

/**
 * Builder for the Kafka Streams configuration.
 */
public abstract class KafkaStreamsBuilder {

  // Kafkastreams application specific
  private String applicationName; // NOPMD
  private String applicationVersion; // NOPMD
  private String bootstrapServers; // NOPMD
  private int numThreads = -1; // NOPMD
  private int commitIntervalMs = -1; // NOPMD
  private int cacheMaxBytesBuff = -1; // NOPMD

  /**
   * Sets the application name for the {@code KafkaStreams} application. It is used to create the
   * application ID.
   *
   * @param applicationName Name of the application.
   * @return
   */
  public KafkaStreamsBuilder applicationName(final String applicationName) {
    this.applicationName = applicationName;
    return this;
  }

  /**
   * Sets the application version for the {@code KafkaStreams} application. It is used to create the
   * application ID.
   *
   * @param applicationVersion Version of the application.
   * @return
   */
  public KafkaStreamsBuilder applicationVersion(final String applicationVersion) {
    this.applicationVersion = applicationVersion;
    return this;
  }

  /**
   * Sets the bootstrap servers for the {@code KafkaStreams} application.
   *
   * @param bootstrapServers String for a bootstrap server.
   * @return
   */
  public KafkaStreamsBuilder bootstrapServers(final String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
    return this;
  }

  /**
   * Sets the Kafka Streams property for the number of threads (num.stream.threads). Can be minus
   * one for using the default.
   *
   * @param numThreads Number of threads. -1 for using the default.
   * @return
   */
  public KafkaStreamsBuilder numThreads(final int numThreads) {
    if (numThreads < -1 || numThreads == 0) {
      throw new IllegalArgumentException("Number of threads must be greater 0 or -1.");
    }
    this.numThreads = numThreads;
    return this;
  }

  /**
   * Sets the Kafka Streams property for the frequency with which to save the position (offsets in
   * source topics) of tasks (commit.interval.ms). Must be zero for processing all record, for
   * example, when processing bulks of records. Can be minus one for using the default.
   *
   * @param commitIntervalMs Frequency with which to save the position of tasks. In ms, -1 for using
   *        the default.
   * @return
   */
  public KafkaStreamsBuilder commitIntervalMs(final int commitIntervalMs) {
    if (commitIntervalMs < -1) {
      throw new IllegalArgumentException("Commit interval must be greater or equal -1.");
    }
    this.commitIntervalMs = commitIntervalMs;
    return this;
  }

  /**
   * Sets the Kafka Streams property for maximum number of memory bytes to be used for record caches
   * across all threads (cache.max.bytes.buffering). Must be zero for processing all record, for
   * example, when processing bulks of records. Can be minus one for using the default.
   *
   * @param cacheMaxBytesBuffering Number of memory bytes to be used for record caches across all
   *        threads. -1 for using the default.
   * @return
   */
  public KafkaStreamsBuilder cacheMaxBytesBuffering(final int cacheMaxBytesBuffering) {
    if (cacheMaxBytesBuffering < -1) {
      throw new IllegalArgumentException("Cache max bytes buffering must be greater or equal -1.");
    }
    this.cacheMaxBytesBuff = cacheMaxBytesBuffering;
    return this;
  }

  /**
   * Method to implement a {@link Topology} for a {@code KafkaStreams} application.
   *
   * @return A {@code Topology} for a {@code KafkaStreams} application.
   */
  protected abstract Topology buildTopology();

  /**
   * Build the {@link Properties} for a {@code KafkaStreams} application.
   *
   * @return A {@code Properties} object.
   */
  protected Properties buildProperties() {
    return PropertiesBuilder
        .bootstrapServers(this.bootstrapServers)
        .applicationId(this.applicationName + '-' + this.applicationVersion)
        .set(StreamsConfig.NUM_STREAM_THREADS_CONFIG, this.numThreads, p -> p > 0)
        .set(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, this.commitIntervalMs, p -> p >= 0)
        .set(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, this.cacheMaxBytesBuff, p -> p >= 0)
        .build();
  }

  /**
   * Builds the {@link KafkaStreams} instance.
   */
  public KafkaStreams build() {
    // Check for required attributes for building properties.
    Objects.requireNonNull(this.bootstrapServers, "Bootstrap server has not been set.");
    Objects.requireNonNull(this.applicationName, "Application name has not been set.");
    Objects.requireNonNull(this.applicationVersion, "Application version has not been set.");

    // Create the Kafka streams instance.
    return new KafkaStreams(this.buildTopology(), this.buildProperties());
  }

}
