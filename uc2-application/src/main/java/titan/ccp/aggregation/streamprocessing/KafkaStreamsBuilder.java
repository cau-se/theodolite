package titan.ccp.aggregation.streamprocessing;

import java.time.Duration;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;

/**
 * Builder for the Kafka Streams configuration.
 */
public class KafkaStreamsBuilder { // NOPMD builder method

  private static final String APPLICATION_NAME = "titan-ccp-aggregation";
  private static final String APPLICATION_VERSION = "0.0.1";

  private static final Duration WINDOW_SIZE_DEFAULT = Duration.ofSeconds(1);
  private static final Duration GRACE_PERIOD_DEFAULT = Duration.ZERO;

  // private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsBuilder.class);

  private String bootstrapServers; // NOPMD
  private String inputTopic; // NOPMD
  private String outputTopic; // NOPMD
  private String configurationTopic; // NOPMD
  private Duration windowSize = null; // NOPMD
  private Duration gracePeriod = null; // NOPMD
  private int numThreads = -1; // NOPMD
  private int commitIntervalMs = -1; // NOPMD
  private int cacheMaxBytesBuffering = -1; // NOPMD

  public KafkaStreamsBuilder inputTopic(final String inputTopic) {
    this.inputTopic = inputTopic;
    return this;
  }

  public KafkaStreamsBuilder outputTopic(final String outputTopic) {
    this.outputTopic = outputTopic;
    return this;
  }

  public KafkaStreamsBuilder configurationTopic(final String configurationTopic) {
    this.configurationTopic = configurationTopic;
    return this;
  }

  public KafkaStreamsBuilder windowSize(final Duration windowSize) {
    this.windowSize = Objects.requireNonNull(windowSize);
    return this;
  }

  public KafkaStreamsBuilder gracePeriod(final Duration gracePeriod) {
    this.gracePeriod = Objects.requireNonNull(gracePeriod);
    return this;
  }

  public KafkaStreamsBuilder bootstrapServers(final String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
    return this;
  }

  /**
   * Sets the Kafka Streams property for the number of threads (num.stream.threads). Can be minus
   * one for using the default.
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
   */
  public KafkaStreamsBuilder cacheMaxBytesBuffering(final int cacheMaxBytesBuffering) {
    if (cacheMaxBytesBuffering < -1) {
      throw new IllegalArgumentException("Cache max bytes buffering must be greater or equal -1.");
    }
    this.cacheMaxBytesBuffering = cacheMaxBytesBuffering;
    return this;
  }

  /**
   * Builds the {@link KafkaStreams} instance.
   */
  public KafkaStreams build() {
    Objects.requireNonNull(this.inputTopic, "Input topic has not been set.");
    Objects.requireNonNull(this.outputTopic, "Output topic has not been set.");
    Objects.requireNonNull(this.configurationTopic, "Configuration topic has not been set.");
    // TODO log parameters
    final TopologyBuilder topologyBuilder = new TopologyBuilder(
        this.inputTopic,
        this.outputTopic,
        this.configurationTopic,
        this.windowSize == null ? WINDOW_SIZE_DEFAULT : this.windowSize,
        this.gracePeriod == null ? GRACE_PERIOD_DEFAULT : this.gracePeriod);
    return new KafkaStreams(topologyBuilder.build(), this.buildProperties());
  }

  private Properties buildProperties() {
    final Properties properties = new Properties();
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG,
        APPLICATION_NAME + '-' + APPLICATION_VERSION); // TODO as parameter
    if (this.numThreads > 0) {
      properties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, this.numThreads);
    }
    if (this.commitIntervalMs >= 0) {
      properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, this.commitIntervalMs);
    }
    if (this.cacheMaxBytesBuffering >= 0) {
      properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, this.cacheMaxBytesBuffering);
    }
    return properties;
  }

}
