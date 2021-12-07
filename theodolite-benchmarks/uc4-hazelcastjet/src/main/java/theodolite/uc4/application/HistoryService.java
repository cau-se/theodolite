package theodolite.uc4.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryService.class);
  // General Information (default)
  private static final String HZ_KUBERNETES_SERVICE_DNS_KEY = "service-dns";
  private static final String BOOTSTRAP_SERVER_DEFAULT = "localhost:5701";
  private static final String SCHEMA_REGISTRY_URL_DEFAULT = "http://localhost:8081";
  private static final String KAFKA_INPUT_TOPIC_DEFAULT = "input";
  private static final String KAFKA_OUTPUT_TOPIC_DEFAULT = "output";
  private static final String KAFKA_BSERVER_DEFAULT = "localhost:19092";
  // UC4 specific (default)
  private static final String KAFKA_CONFIG_TOPIC_DEFAULT = "configuration";
  private static final String KAFKA_FEEDBACK_TOPIC_DEFAULT = "aggregation-feedback";
  private static final String WINDOW_SIZE_DEFAULT = "5000";

  // -- (default) job name for this history serivce
  private static final String JOB_NAME = "uc4-hazelcastjet";

  /**
   * Entrypoint for UC4 using Gradle Run.
   */
  public static void main(final String[] args) {
    final HistoryService uc4HistoryService = new HistoryService();
    try {
      uc4HistoryService.run();
    } catch (final Exception e) { // NOPMD
      e.printStackTrace(); // NOPMD
      System.out.println("An Exception occured. "// NOPMD
          + "No history service is deployed! ABORT MISSION!");
    }
  }

  /**
   * Start a UC4 service.
   *
   * @throws Exception This Exception occurs if the Uc4HazelcastJetFactory is used in the wrong way.
   *         Detailed data is provided once an Exception occurs.
   */
  public void run() throws Exception { // NOPMD
    this.createHazelcastJetApplication();
  }

  /**
   * Creates a Hazelcast Jet Application for UC4 using the Uc1HazelcastJetFactory.
   *
   * @throws Exception This Exception occurs if the Uc4HazelcastJetFactory is used in the wrong way.
   *         Detailed data is provided once an Exception occurs.
   */
  private void createHazelcastJetApplication() throws Exception { // NOPMD
    new Uc4HazelcastJetFactory()
        .setReadPropertiesFromEnv(KAFKA_BSERVER_DEFAULT, SCHEMA_REGISTRY_URL_DEFAULT)
        .setWritePropertiesFromEnv(KAFKA_BSERVER_DEFAULT)
        .setKafkaInputTopicFromEnv(KAFKA_INPUT_TOPIC_DEFAULT)
        .setKafkaOutputTopicFromEnv(KAFKA_OUTPUT_TOPIC_DEFAULT)
        .setKafkaConfigurationTopicFromEnv(KAFKA_CONFIG_TOPIC_DEFAULT)
        .setKafkaFeedbackTopicFromEnv(KAFKA_FEEDBACK_TOPIC_DEFAULT)
        .setWindowSizeFromEnv(WINDOW_SIZE_DEFAULT)
        .buildUc4JetInstanceFromEnv(LOGGER, BOOTSTRAP_SERVER_DEFAULT, HZ_KUBERNETES_SERVICE_DNS_KEY)
        .buildUc4Pipeline()        
        .runUc4Job(JOB_NAME);
  }

}
