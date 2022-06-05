package rocks.theodolite.benchmarks.uc3.hazelcastjet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A microservice that manages the history and, therefore, stores and aggregates incoming
 * measurements.
 */
public class HistoryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryService.class);

  // Hazelcast settings (default)
  private static final String HZ_KUBERNETES_SERVICE_DNS_KEY = "service-dns";
  private static final String BOOTSTRAP_SERVER_DEFAULT = "localhost:5701";

  // Kafka settings (default)
  private static final String KAFKA_BOOTSTRAP_DEFAULT = "localhost:9092";
  private static final String SCHEMA_REGISTRY_URL_DEFAULT = "http://localhost:8081";
  private static final String KAFKA_INPUT_TOPIC_DEFAULT = "input";
  private static final String KAFKA_OUTPUT_TOPIC_DEFAULT = "output";
  
  // UC3 specific (default)
  private static final String WINDOW_SIZE_IN_SECONDS_DEFAULT = "30";
  private static final String HOPSIZE_IN_SEC_DEFAULT = "1";

  // Job name (default)
  private static final String JOB_NAME = "uc3-hazelcastjet";

  /**
   * Entrypoint for UC3 using Gradle Run.
   */
  public static void main(final String[] args) {
    final HistoryService uc3HistoryService = new HistoryService();
    try {
      uc3HistoryService.run();
    } catch (final Exception e) { // NOPMD
      LOGGER.error("ABORT MISSION!: {}", e);
    }
  }

  /**
   * Start a UC3 service.
   *
   * @throws Exception This Exception occurs if the Uc3HazelcastJetFactory is used in the wrong way.
   *         Detailed data is provided once an Exception occurs.
   */
  public void run() throws Exception { // NOPMD
    this.createHazelcastJetApplication();
  }

  /**
   * Creates a Hazelcast Jet Application for UC3 using the Uc3HazelcastJetFactory.
   *
   * @throws Exception This Exception occurs if the Uc3HazelcastJetFactory is used in the wrong way.
   *         Detailed data is provided once an Exception occurs.
   */
  private void createHazelcastJetApplication() throws Exception { // NOPMD
    new Uc3HazelcastJetFactory()
        .setReadPropertiesFromEnv(KAFKA_BOOTSTRAP_DEFAULT, SCHEMA_REGISTRY_URL_DEFAULT, JOB_NAME)
        .setWritePropertiesFromEnv(KAFKA_BOOTSTRAP_DEFAULT, SCHEMA_REGISTRY_URL_DEFAULT)
        .setKafkaInputTopicFromEnv(KAFKA_INPUT_TOPIC_DEFAULT)
        .setKafkaOutputTopicFromEnv(KAFKA_OUTPUT_TOPIC_DEFAULT)
        .setWindowSizeInSecondsFromEnv(WINDOW_SIZE_IN_SECONDS_DEFAULT)
        .setHoppingSizeInSecondsFromEnv(HOPSIZE_IN_SEC_DEFAULT)
        .buildUc3Pipeline()
        .buildUc3JetInstanceFromEnv(LOGGER, BOOTSTRAP_SERVER_DEFAULT, HZ_KUBERNETES_SERVICE_DNS_KEY)
        .runUc3Job(JOB_NAME);
  }

}
