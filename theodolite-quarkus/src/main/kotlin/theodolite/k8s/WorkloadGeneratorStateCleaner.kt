package theodolite.k8s

import mu.KotlinLogging
import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooKeeper
import java.time.Duration

private val logger = KotlinLogging.logger {}

/**
 * Resets the Workloadgenerator states in Zookeeper (and potentially watches for Zookeeper events)
 *
 * @param connectionString of zookeeper
 */
class WorkloadGeneratorStateCleaner(connectionString: String) {
    private val timeout: Duration = Duration.ofMillis(500)
    private val retryAfter: Duration = Duration.ofSeconds(5)
    lateinit var zookeeperClient: ZooKeeper
    private val path = "/workload-generation"

    init {
        try {
            val watcher: Watcher = ZookeeperWatcher() // defined below
            zookeeperClient = ZooKeeper(connectionString, timeout.toMillis().toInt(), watcher)
        } catch (e: Exception) {
            logger.error { e.toString() }
        }
    }

    fun deleteState() {
        deleteRecursiveAll(this.path)
        logger.info { "ZooKeeper reset was successful" }
    }

    /**
     * Deletes a Zookeeper node and its children with the corresponding path.
     */
    private fun deleteRecursiveAll(nodePath: String) {

        while (true) {
            var children: List<String>
            try {
                children = zookeeperClient.getChildren(nodePath, true)
            } catch (e: KeeperException.NoNodeException) {
                break;
            }
            // recursively delete all children nodes
            for (s: String in children) {
                try {
                    deleteRecursiveAll("$nodePath/$s")
                } catch (ex: Exception) {
                    logger.info { "$ex" }
                }
            }

            // delete main node
            try {
                zookeeperClient.delete(nodePath, -1)
                break;
            } catch (ex: Exception) {
                // no instance of node found
                if (ex is KeeperException.NoNodeException) {
                    break;
                } else {
                    logger.error { ex.toString() }
                }
            }
            Thread.sleep(retryAfter.toMillis())
            logger.info { "ZooKeeper reset was not successful. Retrying in 5s" }
        }
    }

    /**
     * Currently empty, could be used to watch(and react) on certain zookeeper events
     */
    private class ZookeeperWatcher : Watcher {

        override fun process(event: WatchedEvent) {}
    }
}
