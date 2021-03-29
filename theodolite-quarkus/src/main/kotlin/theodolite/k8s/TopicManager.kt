package theodolite.k8s

import mu.KotlinLogging
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.ListTopicsResult
import org.apache.kafka.clients.admin.NewTopic

private val logger = KotlinLogging.logger {}

/**
 * Manages the topics related tasks
 * @param kafkaConfig Kafka Configuration as HashMap
 * @constructor creates a KafkaAdminClient
 */
class TopicManager(kafkaConfig: HashMap<String, Any>) {
    private lateinit var kafkaAdmin: AdminClient

    /**
     * Creates a KafkaAdminClient.
     * Logs exception if kafkaConfig was incorrect.
     */
    init {
        try {
            kafkaAdmin = AdminClient.create(kafkaConfig)
        } catch (e: Exception) {
            logger.error { e.toString() }
        }
    }

    /**
     * Creates topics.
     * @param newTopics List of all Topic that should be created
     */
    fun createTopics(newTopics: Collection<NewTopic>) {
        val result = kafkaAdmin.createTopics(newTopics)

        try {
            result.all().get()
        } catch (ex: Exception) {
            logger.error { ex.toString() }
        }
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

    /**
     * Returns all currently available topics.
     */
    fun getTopics(): ListTopicsResult? {
        return kafkaAdmin.listTopics()
    }
}
