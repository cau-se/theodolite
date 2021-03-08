package theodolite.util

import org.apache.kafka.clients.admin.NewTopic
import kotlin.properties.Delegates

class KafkaConfig() {
    lateinit var bootstrapServer: String
    lateinit var topics: List<TopicWrapper>

    fun getKafkaTopics(): List<NewTopic> {
        return topics.map { topic -> NewTopic(topic.name, topic.numPartitions, topic.replicationFactor) }
    }

    class TopicWrapper {
        lateinit var name: String
        var numPartitions by Delegates.notNull<Int>()
        var replicationFactor by Delegates.notNull<Short>()
    }
}