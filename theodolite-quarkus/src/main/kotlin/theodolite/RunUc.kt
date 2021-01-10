package theodolite

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.ListTopicsResult
import org.apache.kafka.clients.admin.NewTopic
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooKeeper
import java.util.*
import org.apache.zookeeper.WatchedEvent




class RunUc (){
    val bootstrapServer = "my-confluent-cp-zookeeper:2181"
    val ip = "172.18.0.9:5556"
    val props = hashMapOf<String, Any>(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to "172.18.0.9:5556")
    lateinit var kafkaAdmin: AdminClient

    init {
        try {
            kafkaAdmin = AdminClient.create(props)
        }
        catch (e: Exception) {
            System.out.println(e.toString())

        }
    }
    fun waitExecution(executionMinutes: Int) {
    var milliToMinutes = 60000
    System.out.println("Wait while executing")
    for (i in 1.rangeTo(executionMinutes)) {
       Thread.sleep((milliToMinutes*i).toLong());
       System.out.println("Executed: "+i.toString()+" minutes")
    }

    System.out.println("Execution finished")
 }

    fun createTopics(topics: Map<String, Int>,replicationfactor: Short) {

        val newTopics = mutableSetOf<NewTopic>()
        for (i in topics) {
            val tops = NewTopic(i.key,i.value,replicationfactor)
            newTopics.add(tops)
        }
        kafkaAdmin.createTopics(newTopics)
        System.out.println("Topics created")
    }

    fun deleteTopics(topics: List<String>) {

        var result  = kafkaAdmin.deleteTopics(topics)
        System.out.println(result.values().toString())

    }

    fun getTopics(): ListTopicsResult? {
        return kafkaAdmin.listTopics()

    }

    fun resetZookeeper(){
        val watcher :Watcher = startWatcher()

        val zookeeperclient = ZooKeeper(ip,60, watcher)
        zookeeperclient.delete("/workload-generation", -1)
        System.out.println("Deletion executed")
    }

    private fun startWatcher(): Watcher {
        return Watcher { event ->
            System.out.println(event.toString())
            System.out.println(event.state.toString())
        }
    }


    fun start_workload_generator(wg: String, dim_value:Integer, uc_id: String){

    }
}
