package theodolite.uc2.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A microservice that manages the history and, therefore, stores and aggregates incoming
 * measurements.
 */
public class HistoryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryService.class);
  // General Information (default)
  private static final String HZ_KUBERNETES_SERVICE_DNS_KEY = "service-dns";
  private static final String BOOTSTRAP_SERVER_DEFAULT = "localhost:5701";
  private static final String SCHEMA_REGISTRY_URL_DEFAULT = "http://localhost:8081";
  private static final String KAFKA_INPUT_TOPIC_DEFAULT = "input";
  private static final String KAFKA_OUTPUT_TOPIC_DEFAULT = "output";
  private static final String KAFKA_BSERVER_DEFAULT = "localhost:19092";
  // UC2 specific (default)
  private static final String DOWNSAMPLE_INTERVAL_DEFAULT = "5000";
  // -- (default) job name for this history serivce
  private static final String JOB_NAME = "uc2-hazelcastjet";

  /**
   * Entrypoint for UC2 using Gradle Run.
   */
  public static void main(final String[] args) {
    final HistoryService uc2HistoryService = new HistoryService();
    try {
      uc2HistoryService.run();
    } catch (final Exception e) { // NOPMD
      e.printStackTrace(); // NOPMD
      System.out.println("An Exception occured. "// NOPMD
          + "No history service is deployed! ABORT MISSION!");
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
        .setReadPropertiesFromEnv(KAFKA_BSERVER_DEFAULT, SCHEMA_REGISTRY_URL_DEFAULT)
        .setWritePropertiesFromEnv(KAFKA_BSERVER_DEFAULT)
        .setKafkaInputTopicFromEnv(KAFKA_INPUT_TOPIC_DEFAULT)
        .setKafkaOutputTopicFromEnv(KAFKA_OUTPUT_TOPIC_DEFAULT)
        .setDownsampleIntervalFromEnv(DOWNSAMPLE_INTERVAL_DEFAULT)
        .buildUc2Pipeline()
        .buildUc2JetInstanceFromEnv(LOGGER, BOOTSTRAP_SERVER_DEFAULT, HZ_KUBERNETES_SERVICE_DNS_KEY)
        .runUc2Job(JOB_NAME);
  }

}
