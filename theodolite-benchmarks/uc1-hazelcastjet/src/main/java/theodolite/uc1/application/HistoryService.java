package theodolite.uc1.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A microservice that manages the history and, therefore, stores and aggregates incoming
 * measurements.
 */
public class HistoryService {

  // Default Values of data used in UC1
  // -- (default) kafkaPropeties data
  private static final String SCHEMA_REGISTRY_URL_DEFAULT = "http://localhost:8081";
  private static final String KAFKA_BOOTSTRAP_SERVERS_DEFAULT = "localhost:19092"; // NOPMD
  // -- (default) JetInstance data
  private static final String HZ_KUBERNETES_SERVICE_DNS_KEY = "service-dns";
  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryService.class);
  private static final String BOOTSTRAP_SERVER_DEFAULT = "localhost:5701";
  // -- (default) Kafka input topic data
  private static final String KAFKA_TOPIC_DEFAULT = "input";
  // -- (default) job name for this history serivce
  private static final String JOB_NAME = "uc1-hazelcastjet";


  /**
   * Entrypoint for UC1 using Gradle Run.
   */
  public static void main(final String[] args) {
    final HistoryService uc1HistoryService = new HistoryService();
    try {
      uc1HistoryService.run();
    } catch (final Exception e) { // NOPMD
      LOGGER.error("ABORT MISSION!: {}", e);
    }
  }

  /**
   * Start a UC1 service.
   *
   * @throws Exception This Exception occurs if the Uc1HazelcastJetFactory is used in the wrong way.
   *         Detailed data is provided once an Exception occurs.
   */
  public void run() throws Exception { // NOPMD
    this.createHazelcastJetApplication();
  }

  /**
   * Creates a Hazelcast Jet Application for UC1 using the Uc1HazelcastJetFactory.
   *
   * @throws Exception This Exception occurs if the Uc1HazelcastJetFactory is used in the wrong way.
   *         Detailed data is provided once an Exception occurs.
   */
  private void createHazelcastJetApplication() throws Exception { // NOPMD
    new Uc1HazelcastJetFactory()
        .setPropertiesFromEnv(KAFKA_BOOTSTRAP_SERVERS_DEFAULT, SCHEMA_REGISTRY_URL_DEFAULT)
        .setKafkaInputTopicFromEnv(KAFKA_TOPIC_DEFAULT)
        .buildUc1Pipeline()
        .buildUc1JetInstanceFromEnv(LOGGER, BOOTSTRAP_SERVER_DEFAULT, HZ_KUBERNETES_SERVICE_DNS_KEY)
        .runUc1Job(JOB_NAME);
  }

}
