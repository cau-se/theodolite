package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection
import org.apache.kafka.clients.admin.NewTopic
import kotlin.properties.Delegates

/**
 * Configuration of Kafka connection.
 *
 * @see TopicWrapper
 */
@RegisterForReflection
class KafkaConfig {
    /**
     * The bootstrap server connection string
     */
    lateinit var bootstrapServer: String

    /**
     * The list of topics
     */
    lateinit var topics: List<TopicWrapper>

    /**
     * Get all current Kafka topics.
     *
     * @return the list of topics.
     */
    fun getKafkaTopics(): List<NewTopic> {
        return topics.map { topic -> NewTopic(topic.name, topic.numPartitions, topic.replicationFactor) }
    }

    /**
     * Wrapper for a topic definition.
     */
    @RegisterForReflection
    class TopicWrapper {
        /**
         * The topic name
         */
        lateinit var name: String

        /**
         * The number of partitions
         */
        var numPartitions by Delegates.notNull<Int>()

        /**
         * The replication factor of this topic
         */
        var replicationFactor by Delegates.notNull<Short>()
    }
}
