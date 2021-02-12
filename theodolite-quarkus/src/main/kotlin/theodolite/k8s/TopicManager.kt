package theodolite.k8s

import mu.KotlinLogging
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.ListTopicsResult
import org.apache.kafka.clients.admin.NewTopic

private val logger = KotlinLogging.logger {}

/**
 * Manages the topics related tasks
 * @param bootstrapServers Ip of the kafka server
 */
class TopicManager(kafkaConfig: HashMap<String, Any>) {
    lateinit var kafkaAdmin: AdminClient

    init {
        try {
            kafkaAdmin = AdminClient.create(kafkaConfig)
        } catch (e: Exception) {
            logger.error { e.toString() }
        }
    }

    /**
     * Creates topics.
     * @param topics Map that holds a numPartition for each topic it should create
     * @param replicationFactor
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
