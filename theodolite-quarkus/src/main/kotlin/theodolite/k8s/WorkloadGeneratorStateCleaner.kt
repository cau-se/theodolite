package theodolite.k8s

import mu.KotlinLogging
import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooKeeper

private val logger = KotlinLogging.logger {}


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
            logger.error {e.toString()}
        }
    }

    fun deleteAll() {
        var deleted = false
        while (!deleted) {

            //
            try {
                zookeeperClient.delete(path, -1)
            } catch (ex: Exception) {
                logger.error {ex.toString()}
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
                        logger.error {ex.toString()}
                    }
                }
            }
            Thread.sleep(retryTime)
            logger.info {"ZooKeeper reset was not successful. Retrying in 5s"}
        }

        logger.info {"ZooKeeper reset was successful"}
    }

    private class ZookeperWatcher : Watcher {

        override fun process(event: WatchedEvent) {}
    }
}
