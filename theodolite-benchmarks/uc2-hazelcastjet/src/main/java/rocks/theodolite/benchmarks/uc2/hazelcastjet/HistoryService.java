package rocks.theodolite.benchmarks.uc2.hazelcastjet;

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

  // UC2 specific (default)
  private static final String DOWNSAMPLE_INTERVAL_DEFAULT_MS = "60000";

  // Job name (default)
  private static final String JOB_NAME = "uc2-hazelcastjet";

  /**
   * Entrypoint for UC2 using Gradle Run.
   */
  public static void main(final String[] args) {
    final HistoryService uc2HistoryService = new HistoryService();
    try {
      uc2HistoryService.run();
    } catch (final Exception e) { // NOPMD
      LOGGER.error("ABORT MISSION!: {}", e);
    }
  }

  /**
   * Start a UC2 service.
   *
   * @throws Exception This Exception occurs if the Uc2HazelcastJetFactory is used in the wrong way.
   *         Detailed data is provided once an Exception occurs.
   */
  public void run() throws Exception { // NOPMD
    this.createHazelcastJetApplication();
  }

  /**
   * Creates a Hazelcast Jet Application for UC2 using the Uc1HazelcastJetFactory.
   *
   * @throws Exception This Exception occurs if the Uc2HazelcastJetFactory is used in the wrong way.
   *         Detailed data is provided once an Exception occurs.
   */
  private void createHazelcastJetApplication() throws Exception { // NOPMD
    new Uc2HazelcastJetFactory()
        .setReadPropertiesFromEnv(KAFKA_BOOTSTRAP_DEFAULT, SCHEMA_REGISTRY_URL_DEFAULT)
        .setWritePropertiesFromEnv(KAFKA_BOOTSTRAP_DEFAULT)
        .setKafkaInputTopicFromEnv(KAFKA_INPUT_TOPIC_DEFAULT)
        .setKafkaOutputTopicFromEnv(KAFKA_OUTPUT_TOPIC_DEFAULT)
        .setDownsampleIntervalFromEnv(DOWNSAMPLE_INTERVAL_DEFAULT_MS)
        .buildUc2Pipeline()
        .buildUc2JetInstanceFromEnv(LOGGER, BOOTSTRAP_SERVER_DEFAULT, HZ_KUBERNETES_SERVICE_DNS_KEY)
        .runUc2Job(JOB_NAME);
  }

}
