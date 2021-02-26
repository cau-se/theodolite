package theodolite.util

import org.apache.kafka.clients.admin.NewTopic

class KafkaConfig() {
    lateinit var bootstrapSever: String
    lateinit var topics: List<NewTopic>

}