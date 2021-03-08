package theodolite.util

import org.apache.kafka.clients.admin.NewTopic

class KafkaConfig() {
    lateinit var bootstrapServer: String
    lateinit var topics: List<NewTopic>
}