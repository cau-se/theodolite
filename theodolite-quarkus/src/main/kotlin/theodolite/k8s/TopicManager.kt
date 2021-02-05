package theodolite.k8s

import mu.KotlinLogging
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.ListTopicsResult
import org.apache.kafka.clients.admin.NewTopic

private val logger = KotlinLogging.logger {}

class TopicManager(bootstrapServers: String) {
    private val props = hashMapOf<String, Any>(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers)
    lateinit var kafkaAdmin: AdminClient

    init {
        try {
            kafkaAdmin = AdminClient.create(props)
        } catch (e: Exception) {
            logger.error {e.toString()}
        }
    }

    fun createTopics(topics: Map<String, Int>, replicationFactor: Short) {

        val newTopics = mutableSetOf<NewTopic>()
        for (i in topics) {
            val tops = NewTopic(i.key, i.value, replicationFactor)
            newTopics.add(tops)
        }
        kafkaAdmin.createTopics(newTopics)
        logger.info {"Topics created"}
    }

    fun createTopics(topics: List<String>, numPartitions: Int, replicationFactor: Short) {

        val newTopics = mutableSetOf<NewTopic>()
        for (i in topics) {
            val tops = NewTopic(i, numPartitions, replicationFactor)
            newTopics.add(tops)
        }
        kafkaAdmin.createTopics(newTopics)
        logger.info {"Creation of $topics started"}
    }

    fun deleteTopics(topics: List<String>) {

        val result = kafkaAdmin.deleteTopics(topics)

        try {
            result.all().get()
        } catch (ex: Exception) {
            logger.error {ex.toString()}
        }
        logger.info {"Topics deleted"}
    }

    fun getTopics(): ListTopicsResult? {
        return kafkaAdmin.listTopics()
    }
}
