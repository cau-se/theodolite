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
 * @param kafkaConfig Kafka configuration as a Map
 * @constructor Creates a KafkaAdminClient
 */
class TopicManager(private val kafkaConfig: Map<String, Any>) {

    /**
     * Create topics.
     * @param newTopics Collection of all topic that should be created
     */
    fun createTopics(newTopics: Collection<NewTopic>) {
        val kafkaAdmin: AdminClient = AdminClient.create(this.kafkaConfig)
        lateinit var result: CreateTopicsResult

        do {
            var retryCreation = false
            try {
                result = kafkaAdmin.createTopics(newTopics)
                result.all().get() // wait for the future to be completed

            } catch (e: Exception) {
                logger.warn(e) { "Error during topic creation." }
                logger.debug { e } // TODO remove?
                logger.info { "Remove existing topics." }
                delete(newTopics.map { topic -> topic.name() }, kafkaAdmin)
                logger.info { "Will retry the topic creation in $RETRY_TIME seconds." }
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
     * Remove topics.
     * @param topics Collection of names for the topics to remove.
     */
    fun removeTopics(topics: List<String>) {
        val kafkaAdmin: AdminClient = AdminClient.create(this.kafkaConfig)
        delete(topics, kafkaAdmin)
        kafkaAdmin.close()
    }

    private fun delete(topics: List<String>, kafkaAdmin: AdminClient) {
        var deleted = false

        while (!deleted) {
            try {
                val result = kafkaAdmin.deleteTopics(topics)
                result.all().get() // wait for the future to be completed
                logger.info {
                    "Topics deletion finished with result: ${
                        result.values().map { it -> it.key + ": " + it.value.isDone }
                            .joinToString(separator = ",")
                    }"
                }
            } catch (e: Exception) {
                logger.error(e) { "Error while removing topics: $e" }
                logger.info { "Existing topics are: ${kafkaAdmin.listTopics()}." }
            }

            val toDelete = topics.filter { topic ->
                kafkaAdmin.listTopics().names().get().contains(topic)
            }

            if (toDelete.isNullOrEmpty()) {
                deleted = true
            } else {
                logger.info { "Deletion of kafka topics failed, will retry in $RETRY_TIME seconds." }
                sleep(RETRY_TIME)
            }
        }
    }

}
