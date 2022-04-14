package rocks.theodolite.commons.commons.configuration;

import com.google.common.io.Resources;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for creating {@link Configuration}s.
 */
public final class ServiceConfigurations {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConfigurations.class);

  private static final String DEFAULT_PROPERTIES_LOCATION = "META-INF/application.properties";
  private static final String USER_PROPERTIES_LOCATION = "config/application.properties";

  private ServiceConfigurations() {}

  /**
   * Create a {@link Configuration} as it typically used by microservices. More precisely, this
   * means when trying to access value with a particular key, this configuration first looks for
   * this key in the current environment variables, then in a properties file located in
   * {@code config/application.properties}, and finally for a properties file in the classpath at
   * {@code META-INF/application.properties}.
   *
   * @see NameResolvingEnvironmentConfiguration for details regarding the environemnt variable
   *      lookup.
   */
  public static Configuration createWithDefaults() {
    return builder()
        .withEnvironmentVariables()
        .withUserConfigurationFile(USER_PROPERTIES_LOCATION)
        .withDefaultConfigurationFile(DEFAULT_PROPERTIES_LOCATION)
        .build();
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder class for a {@link Configuration} for a microservice.
   */
  public static class Builder {

    private final CompositeConfiguration configuration = new CompositeConfiguration();

    private Builder() {}

    public Builder withEnvironmentVariables() {
      this.configuration.addConfiguration(new NameResolvingEnvironmentConfiguration());
      return this;
    }

    /**
     * Add a properties file from the user's file system to the {@link Configuration}.
     */
    public Builder withUserConfigurationFile(final String userPropertiesLocation) {
      final Path path = Paths.get(userPropertiesLocation);
      LOGGER.info("Looking for user configuration at {}", userPropertiesLocation);
      if (Files.exists(path)) {
        LOGGER.info("Found user configuration at {}", userPropertiesLocation);
        try {
          this.configuration.addConfiguration(configurations().properties(path.toFile()));
        } catch (final ConfigurationException e) {
          throw new IllegalArgumentException(
              "Cannot load configuration from file '" + userPropertiesLocation + "'", e);
        }
      } else {
        LOGGER.info("No user configuration found at {}", userPropertiesLocation);
      }
      return this;
    }

    /**
     * Add a properties file from the class path to the {@link Configuration}.
     */
    public Builder withDefaultConfigurationFile(final String defaultPropertiesLocation) {
      if (resourceExists(defaultPropertiesLocation)) {
        try {
          this.configuration
              .addConfiguration(configurations().properties(defaultPropertiesLocation));
        } catch (final ConfigurationException e) {
          throw new IllegalArgumentException(
              "Cannot load configuration from ressource '" + defaultPropertiesLocation + "'", e);
        }
      }
      return this;
    }

    public Configuration build() {
      return this.configuration;
    }

  }

  /**
   * Shortcut for long class name.
   */
  private static org.apache.commons.configuration2.builder.fluent.Configurations configurations() {
    // TODO Refactor when Configurations class is removed
    return new org.apache.commons.configuration2.builder.fluent.Configurations();
  }

  private static boolean resourceExists(final String resourceName) {
    try {
      Resources.getResource(resourceName);
    } catch (final IllegalArgumentException e) {
      return false;
    }
    return true;
  }

}
