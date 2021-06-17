package theodolite.k8s

import mu.KotlinLogging
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.CreateTopicsResult
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.errors.TopicExistsException
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
            } catch (e: Exception) { // TopicExistsException
                logger.warn(e) { "Error during topic creation." }
                logger.debug { e } // TODO remove due to attached exception to warn log?
                logger.info { "Remove existing topics." }
                delete(newTopics.map { topic -> topic.name() }, kafkaAdmin)
                logger.info { "Will retry the topic creation in ${RETRY_TIME/1000} seconds." }
                sleep(RETRY_TIME)
                retryCreation = true
            }
        } while (retryCreation)

        logger.info {
            "Topics creation finished with result: ${
                result
                    .values()
                    .map { it.key + ": " + it.value.isDone }
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
        val currentTopics = kafkaAdmin.listTopics().names().get()
        delete(currentTopics.filter { matchRegex(it, topics) }, kafkaAdmin)
        kafkaAdmin.close()
    }

    /**
     * This function checks whether one string in `topics` can be used as prefix of a regular expression
     * to create the string `existingTopic`.
     *
     * @param existingTopic string for which should be checked if it could be created.
     * @param topics list of string which are used as possible prefixes to create `existingTopic`.
     * @return true, `existingTopics` matches a created regex, else false.
     */
    private fun matchRegex(existingTopic: String, topics: List<String>): Boolean {
        for (t in topics) {
            val regex = t.toRegex()
            if (regex.matches(existingTopic)) {
                return true
            }
        }
        return false
    }

    private fun delete(topics: List<String>, kafkaAdmin: AdminClient) {
        var deleted = false

        while (!deleted) {
            try {
                val result = kafkaAdmin.deleteTopics(topics)
                result.all().get() // wait for the future to be completed
                logger.info {
                    "Topics deletion finished with result: ${
                        result.values().map { it.key + ": " + it.value.isDone }
                            .joinToString(separator = ",")
                    }"
                }
            } catch (e: Exception) {
                logger.error(e) { "Error while removing topics: $e" }
                logger.info { "Existing topics are: ${kafkaAdmin.listTopics().names().get()}." }
            }

            val toDelete = topics.filter { kafkaAdmin.listTopics().names().get().contains(it) }

            if (toDelete.isNullOrEmpty()) {
                deleted = true
            } else {
                logger.info { "Deletion of Kafka topics failed, will retry in ${RETRY_TIME/1000} seconds." }
                sleep(RETRY_TIME)
            }
        }
    }

}
