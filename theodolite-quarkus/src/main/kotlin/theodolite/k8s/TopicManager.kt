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
class TopicManager(kafkaConfig: HashMap<String, Any>) {
    private lateinit var kafkaAdmin: AdminClient

    init {
        try {
            kafkaAdmin = AdminClient.create(kafkaConfig)
        } catch (e: Exception) {
            logger.error { e.toString() }
        }
    }

    /**
     * Creates topics.
     * @param newTopics List of all Topic which should be created
     */
    fun createTopics(newTopics: Collection<NewTopic>) {
        kafkaAdmin.createTopics(newTopics)
        logger.info { "Topics created" }
    }

    fun createTopics(topics: List<String>, numPartitions: Int, replicationFactor: Short) {
        val newTopics = mutableSetOf<NewTopic>()
        for (i in topics) {
            val tops = NewTopic(i, numPartitions, replicationFactor)
            newTopics.add(tops)
        }
        kafkaAdmin.createTopics(newTopics)
        logger.info { "Creation of $topics started" }
    }

    /**
     * Removes topics.
     * @param topics
     */
    fun removeTopics(topics: List<String>) {
        val result = kafkaAdmin.deleteTopics(topics)

        try {
            result.all().get()
        } catch (ex: Exception) {
            logger.error { ex.toString() }
        }
        logger.info { "Topics removed" }
    }

    fun getTopics(): ListTopicsResult? {
        return kafkaAdmin.listTopics()
    }
}
