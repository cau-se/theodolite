package rocks.theodolite.benchmarks.commons.kstreams;

import java.util.Objects;
import java.util.Properties;
import java.util.function.Predicate;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation for {@link PropertiesBuilder} and
 * {@link PropertiesBuilder.WithoutApplicationId}.
 */
class PropertiesBuilderImpl implements PropertiesBuilder, PropertiesBuilder.WithoutApplicationId {

  private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesBuilderImpl.class);

  private final Properties properties = new Properties();

  @Override
  public <T> PropertiesBuilderImpl set(final String configKey, final T value) {
    this.properties.put(configKey, value);
    LOGGER.info("Set Kafka Streams configuration parameter '{}' to '{}'.", configKey, value);
    return this;
  }

  @Override
  public <T> PropertiesBuilderImpl set(final String configKey, final T value,
      final Predicate<T> condition) {
    if (condition.test(value)) {
      this.set(configKey, value);
    }
    return this;
  }

  @Override
  public Properties build() {
    return this.properties;
  }

  @Override
  public PropertiesBuilderImpl applicationId(final String applicationId) {
    Objects.requireNonNull(applicationId, "Kafka Streams application ID cannot be null.");
    return this.set(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
  }

  protected static PropertiesBuilderImpl bootstrapServers(final String bootsrapservers) {
    Objects.requireNonNull(bootsrapservers, "Kafka bootstrap servers cannot be null.");
    return new PropertiesBuilderImpl().set(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootsrapservers);
  }

}
