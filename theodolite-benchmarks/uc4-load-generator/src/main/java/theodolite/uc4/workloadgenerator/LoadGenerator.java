package theodolite.uc4.workloadgenerator;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theodolite.commons.workloadgeneration.KeySpace;
import titan.ccp.configuration.events.Event;
import titan.ccp.model.sensorregistry.SensorRegistry;

/**
 * Load generator for Theodolite use case UC4.
 */
public final class LoadGenerator {

  private static final int SLEEP_PERIOD = 30_000;

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadGenerator.class);

  private LoadGenerator() {}

  /**
   * Start load generator.
   */
  public static void main(final String[] args) {
    final boolean sendRegistry = Boolean.parseBoolean(Objects.requireNonNullElse(
        System.getenv("SEND_REGISTRY"),
        "true"));
    final String kafkaBootstrapServers = Objects.requireNonNullElse(
        System.getenv("KAFKA_BOOTSTRAP_SERVERS"),
        "localhost:9092");
    final int numSensors = Integer.parseInt(Objects.requireNonNullElse(
        System.getenv("NUM_SENSORS"),
        "1"));
    final int numNestedGroups = Integer.parseInt(Objects.requireNonNullElse(
        System.getenv("NUM_NESTED_GROUPS"),
        "1"));

    // Build sensor hierarchy
    final SensorRegistry sensorRegistry =
        new SensorRegistryBuilder(numNestedGroups, numSensors).build();

    LOGGER.info("Start workload generator for use case UC4");
    theodolite.commons.workloadgeneration.LoadGenerator.fromEnvironment()
        .withKeySpace(new KeySpace("s_", sensorRegistry.getMachineSensors().size()))
        .withBeforeAction(() -> {
          if (sendRegistry) {
            final ConfigPublisher configPublisher =
                new ConfigPublisher(kafkaBootstrapServers, "configuration");
            configPublisher.publish(Event.SENSOR_REGISTRY_CHANGED, sensorRegistry.toJson());
            configPublisher.close();
            LOGGER.info("Configuration sent.");

            LOGGER.info("Now wait 30 seconds...");
            try {
              Thread.sleep(SLEEP_PERIOD);
            } catch (final InterruptedException e) {
              LOGGER.error(e.getMessage(), e);
            }
            LOGGER.info("...and start generating load.");
          }
        })
        .run();
  }

}
