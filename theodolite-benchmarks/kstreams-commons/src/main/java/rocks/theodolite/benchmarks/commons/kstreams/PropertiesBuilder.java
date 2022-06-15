package rocks.theodolite.benchmarks.commons.kstreams;

import java.util.Properties;
import java.util.function.Predicate;


/**
 * Interface for a helper class for constructing and logging Kafka Stream {@link Properties}.
 */
public interface PropertiesBuilder {

  /**
   * Set the provided configuration key to the provided value.
   */
  <T> PropertiesBuilder set(String configKey, T value);

  /**
   * Set the provided configuration key to the provided value if a given condition is evaluated to
   * true.
   */
  <T> PropertiesBuilder set(String configKey, T value, Predicate<T> condition);

  /**
   * Build a {@link Properties} object with the option being set before.
   */
  Properties build();

  /**
   * Interface representing an Kafka Stream {@link Properties} builder without the application id
   * yet being set.
   */
  interface WithoutApplicationId {

    /**
     * Continue building Kafka Stream properties by specifying an application id. From now on, all
     * configuration properties can be set directly.
     */
    PropertiesBuilder applicationId(String applicationId);

  }

  /**
   * Start building Kafka Stream properties by specifying a bootstrap server. Next, an application
   * id has to be specified.
   */
  static PropertiesBuilder.WithoutApplicationId bootstrapServers(final String bootstrapServers) {
    return PropertiesBuilderImpl.bootstrapServers(bootstrapServers);
  }

}
