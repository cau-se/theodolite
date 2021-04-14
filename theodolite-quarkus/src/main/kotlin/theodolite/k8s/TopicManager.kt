package theodolite.k8s

import mu.KotlinLogging
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.ListTopicsResult
import org.apache.kafka.clients.admin.NewTopic
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Manages the topics related tasks
 * @param kafkaConfig Kafka Configuration as HashMap
 */
class TopicManager(private val kafkaConfig: HashMap<String, Any>) {

    /**
     * Creates topics.
     * @param newTopics List of all Topic which should be created
     */
    fun createTopics(newTopics: Collection<NewTopic>) {
        var kafkaAdmin: AdminClient = AdminClient.create(this.kafkaConfig)
        val result = kafkaAdmin.createTopics(newTopics)
        logger.info { "Topics created finished with result: ${result.all().get()}" }
        kafkaAdmin.close()
    }


    /**
     * Removes topics.
     * @param topics
     */
    fun removeTopics(topics: List<String>) {
        var kafkaAdmin: AdminClient = AdminClient.create(this.kafkaConfig)

        try {
            val result = kafkaAdmin.deleteTopics(topics)
            logger.info { "Topics deletion finished with result: ${result.all().get()}" }
        } catch (e: Exception) {
            logger.error { "Error while removing topics: $e"  }
            logger.debug { "Existing topics are: ${kafkaAdmin.listTopics()}."  }
        }
        kafkaAdmin.close()
        logger.info { "Topics removed" }
    }
}
