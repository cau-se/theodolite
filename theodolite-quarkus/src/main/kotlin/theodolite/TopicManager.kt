package theodolite

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.ListTopicsResult
import org.apache.kafka.clients.admin.NewTopic

class TopicManager(boostrapIp: String) {
    val props = hashMapOf<String, Any>(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to boostrapIp)
    lateinit var kafkaAdmin: AdminClient

    init {
        try {
            kafkaAdmin = AdminClient.create(props)
        } catch (e : Exception) {
            System.out.println(e.toString())
        }
    }

    fun createTopics(topics: Map<String, Int>, replicationfactor: Short) {

        val newTopics = mutableSetOf<NewTopic>()
        for (i in topics) {
            val tops = NewTopic(i.key, i.value, replicationfactor)
            newTopics.add(tops)
        }
        kafkaAdmin.createTopics(newTopics)
        System.out.println("Topics created")
    }

    fun createTopics(topics: List<String>, numPartitions: Int, replicationfactor: Short) {

        val newTopics = mutableSetOf<NewTopic>()
        for (i in topics) {
            val tops = NewTopic(i, numPartitions, replicationfactor)
            newTopics.add(tops)
        }
        kafkaAdmin.createTopics(newTopics)
        System.out.println("Creation of $topics started")
    }

    fun deleteTopics(topics: List<String>) {

        val result = kafkaAdmin.deleteTopics(topics)

        try {
            result.all().get()
        } catch (ex:Exception) {
            System.out.println(ex.toString())
        }
        System.out.println("Topics deleted")
    }

    fun getTopics(): ListTopicsResult? {
        return kafkaAdmin.listTopics()
    }
}
