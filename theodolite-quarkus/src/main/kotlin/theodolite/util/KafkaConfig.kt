package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection
import org.apache.kafka.clients.admin.NewTopic
import kotlin.properties.Delegates

/**
 * Configuration of Kafka connection.
 *
 * @param bootstrapServer the bootstrap server connection string
 * @param topics the list of topics
 *
 * @see TopicWrapper
 */
@RegisterForReflection
class KafkaConfig {
    lateinit var bootstrapServer: String
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
     *
     * @param name the topic name
     * @param numPartitions the number of partitions
     * @param replicationFactor the replication factor of this topic
     */
    @RegisterForReflection
    class TopicWrapper {
        lateinit var name: String
        var numPartitions by Delegates.notNull<Int>()
        var replicationFactor by Delegates.notNull<Short>()
    }
}
