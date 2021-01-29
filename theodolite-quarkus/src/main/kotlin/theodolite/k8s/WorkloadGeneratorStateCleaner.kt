package theodolite.k8s

import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooKeeper

class WorkloadGeneratorStateCleaner(ip: String) {
    val path = "/workload-generation"
    val sessionTimeout = 60
    val retryTime = 3000L
    lateinit var zookeeperClient: ZooKeeper

    init {
        try {
            val watcher: Watcher = ZookeperWatcher()  // defined below
            zookeeperClient = ZooKeeper(ip, sessionTimeout, watcher)
        } catch (e: Exception) {
            System.out.println(e.toString())
        }
    }

    fun deleteAll() {
        var deleted = false
        while (!deleted) {

            //
            try {
                zookeeperClient.delete(path, -1)
            } catch (ex: Exception) {
                System.out.println(ex.toString())
            }

            try {
                val clients = zookeeperClient.getChildren(path, true)
                if (clients.isEmpty()) {
                    break;
                }
            } catch (ex: Exception) {
                when (ex) {
                    is KeeperException -> {
                        deleted = true
                    }
                    is InterruptedException -> {
                        System.out.println(ex.toString())
                    }
                }
            }
            Thread.sleep(retryTime)
            System.out.println("ZooKeeper reset was not successful. Retrying in 5s")
        }

        System.out.println("ZooKeeper reset was successful")
    }

    private class ZookeperWatcher : Watcher {

        override fun process(event: WatchedEvent) {}
    }
}
