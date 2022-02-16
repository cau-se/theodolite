package theodolite.commons.kafkastreams;

import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import titan.ccp.common.kafka.streams.PropertiesBuilder;

/**
 * Builder for the Kafka Streams configuration.
 */
public abstract class KafkaStreamsBuilder {

  // Kafka Streams application specific
  protected final String schemaRegistryUrl; // NOPMD for use in subclass
  protected final String inputTopic; // NOPMD for use in subclass

  private final Configuration config;

  private final String applicationName; // NOPMD
  private final String applicationVersion; // NOPMD
  private final String bootstrapServers; // NOPMD

  /**
   * Construct a new Build object for a Kafka Streams application.
   *
   * @param config Contains the key value pairs for configuration.
   */
  public KafkaStreamsBuilder(final Configuration config) {
    this.config = config;
    this.applicationName = this.config.getString(ConfigurationKeys.APPLICATION_NAME);
    this.applicationVersion = this.config.getString(ConfigurationKeys.APPLICATION_VERSION);
    this.bootstrapServers = this.config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS);
    this.schemaRegistryUrl = this.config.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL);
    this.inputTopic = this.config.getString(ConfigurationKeys.KAFKA_INPUT_TOPIC);
  }

  /**
   * Checks if the given key is contained in the configurations and sets it in the properties.
   *
   * @param <T> Type of the value for given key
   * @param propBuilder Object where to set this property.
   * @param key The key to check and set the property.
   * @param valueGetter Method to get the value from with given key.
   * @param condition for setting the property.
   */
  private <T> void setOptionalProperty(final PropertiesBuilder propBuilder,
      final String key,
      final Function<String, T> valueGetter,
      final Predicate<T> condition) {
    if (this.config.containsKey(key)) {
      final T value = valueGetter.apply(key);
      propBuilder.set(key, value, condition);
    }
  }

  /**
   * Build the {@link Properties} for a {@code KafkaStreams} application.
   *
   * @return A {@code Properties} object.
   */
  protected Properties buildProperties() {
    // required configuration
    final PropertiesBuilder propBuilder = PropertiesBuilder
        .bootstrapServers(this.bootstrapServers)
        .applicationId(this.applicationName + '-' + this.applicationVersion);

    // optional configurations
    this.setOptionalProperty(propBuilder, StreamsConfig.ACCEPTABLE_RECOVERY_LAG_CONFIG,
        this.config::getLong, p -> p >= 0);
    this.setOptionalProperty(propBuilder, StreamsConfig.BUFFERED_RECORDS_PER_PARTITION_CONFIG,
        this.config::getInt, p -> p > 0);
    this.setOptionalProperty(propBuilder, StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG,
        this.config::getInt, p -> p >= 0);
    this.setOptionalProperty(propBuilder, StreamsConfig.COMMIT_INTERVAL_MS_CONFIG,
        this.config::getInt, p -> p >= 0);
    this.setOptionalProperty(propBuilder, StreamsConfig.MAX_TASK_IDLE_MS_CONFIG,
        this.config::getLong, p -> p >= 0);
    this.setOptionalProperty(propBuilder, StreamsConfig.MAX_WARMUP_REPLICAS_CONFIG,
        this.config::getInt, p -> p >= 1);
    this.setOptionalProperty(propBuilder, StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG,
        this.config::getInt, p -> p >= 0);
    this.setOptionalProperty(propBuilder, StreamsConfig.NUM_STREAM_THREADS_CONFIG,
        this.config::getInt, p -> p > 0);
    this.setOptionalProperty(propBuilder, StreamsConfig.POLL_MS_CONFIG,
        this.config::getLong, p -> p >= 0);
    this.setOptionalProperty(propBuilder, StreamsConfig.PROCESSING_GUARANTEE_CONFIG,
        this.config::getString, this::validateProcessingGuarantee);
    this.setOptionalProperty(propBuilder, StreamsConfig.REPLICATION_FACTOR_CONFIG,
        this.config::getInt, p -> p >= 0);

    if (this.config.containsKey(StreamsConfig.TOPOLOGY_OPTIMIZATION_CONFIG)
        && this.config.getBoolean(StreamsConfig.TOPOLOGY_OPTIMIZATION_CONFIG)) {
      propBuilder.set(StreamsConfig.TOPOLOGY_OPTIMIZATION_CONFIG, StreamsConfig.OPTIMIZE);
    }

    return propBuilder.build();
  }

  @SuppressWarnings("deprecation")
  private boolean validateProcessingGuarantee(final String processingGuarantee) {
    return StreamsConfig.AT_LEAST_ONCE.equals(processingGuarantee)
        // We continue support EXACTLY_ONCE to allow benchmarking it against v2
        || StreamsConfig.EXACTLY_ONCE.equals(processingGuarantee)
        || StreamsConfig.EXACTLY_ONCE_V2.equals(processingGuarantee);
  }

  /**
   * Method to implement a {@link Topology} for a {@code KafkaStreams} application.
   *
   * @return A {@code Topology} for a {@code KafkaStreams} application.
   */
  protected abstract Topology buildTopology(Properties properties);

  /**
   * Builds the {@link KafkaStreams} instance.
   */
  public KafkaStreams build() {
    // Create the Kafka Streams instance.
    final Properties properties = this.buildProperties();
    return new KafkaStreams(this.buildTopology(properties), properties);
  }

}
