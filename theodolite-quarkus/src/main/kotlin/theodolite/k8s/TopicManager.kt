package theodolite.k8s

import mu.KotlinLogging
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.CreateTopicsResult
import org.apache.kafka.clients.admin.NewTopic
import java.lang.Thread.sleep

private val logger = KotlinLogging.logger {}
private const val RETRY_TIME = 2000L

/**
 * Manages the topics related tasks
 * @param kafkaConfig Kafka Configuration as HashMap
 * @constructor Creates a KafkaAdminClient
 */
class TopicManager(private val kafkaConfig: HashMap<String, Any>) {
    /**
     * Creates topics.
     * @param newTopics List of all Topic that should be created
     */
    fun createTopics(newTopics: Collection<NewTopic>) {
        val kafkaAdmin: AdminClient = AdminClient.create(this.kafkaConfig)
        lateinit var result: CreateTopicsResult

        do {
            var retryCreation = false
            try {
                result = kafkaAdmin.createTopics(newTopics)
                result.all().get()// wait for the future object

            } catch (e: Exception) {
                logger.warn { "Error during topic creation." }
                logger.warn { "Will retry the topic creation after 2 seconds" }
                sleep(RETRY_TIME)
                retryCreation = true
            }
        } while (retryCreation)

        logger.info {
            "Topics creation finished with result: ${
                result.values().map { it -> it.key + ": " + it.value.isDone }
                    .joinToString(separator = ",")
            } "
        }
        kafkaAdmin.close()
    }

    /**
     * Removes topics.
     * @param topics List of names with the topics to remove.
     */
    fun removeTopics(topics: List<String>) {
        val kafkaAdmin: AdminClient = AdminClient.create(this.kafkaConfig)
        var deleted = false
        
        while (!deleted) {
            try {
                val result = kafkaAdmin.deleteTopics(topics)
                result.all().get() // wait for the future object
                logger.info {
                    "Topics deletion finished with result: ${
                        result.values().map { it -> it.key + ": " + it.value.isDone }
                            .joinToString(separator = ",")
                    }"
                }
            } catch (e: Exception) {
                logger.error { "Error while removing topics: $e" }
                logger.debug { "Existing topics are: ${kafkaAdmin.listTopics()}." }
            }

            val toDelete = topics.filter { topic ->
                kafkaAdmin.listTopics().names().get().contains(topic)
            }

            if (toDelete.isNullOrEmpty()) {
                deleted = true
            } else {
                logger.info { "Deletion of kafka topics failed retrying in 2 seconds" }
                sleep(RETRY_TIME)
            }
        }
        kafkaAdmin.close()
    }
}
