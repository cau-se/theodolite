package theodolite.k8s

import mu.KotlinLogging
import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooKeeper
import java.time.Duration

private val logger = KotlinLogging.logger {}

/**
 * Resets the workloadgenerator states in zookeper (and potetially watches for Zookeper events)
 *
 * @param ip of zookeeper
 * @param path path of the zookeeper node
 */
class WorkloadGeneratorStateCleaner(ip: String, val path: String) {
    private val timeout: Duration = Duration.ofMillis(500)
    private val retryAfter: Duration = Duration.ofSeconds(5)
    lateinit var zookeeperClient: ZooKeeper

    init {
        try {
            val watcher: Watcher = ZookeeperWatcher() // defined below
            zookeeperClient = ZooKeeper(ip, timeout.toMillis().toInt(), watcher)
        } catch (e: Exception) {
            logger.error { e.toString() }
        }
    }

    /**
     * Deletes all Zookeeper nodes with the corresponding path.
     */
    fun deleteAll() {
        var deleted = false
        while (!deleted) {
            try {
                zookeeperClient.delete(this.path, -1)
            } catch (ex: Exception) {
                logger.error { ex.toString() }
            }

            try {
                // get list of all nodes of the given path
                val clients = zookeeperClient.getChildren(this.path, true)
                if (clients.isEmpty()) {
                    deleted = true
                    break
                }
            } catch (ex: Exception) {
                when (ex) {
                    // indicates that there are no nodes to delete left
                    is KeeperException -> {
                        deleted = true
                    }
                    is InterruptedException -> {
                        logger.error { ex.toString() }
                    }
                }
            }
            Thread.sleep(retryAfter.toMillis())
            logger.info { "ZooKeeper reset was not successful. Retrying in 5s" }
        }
        logger.info { "ZooKeeper reset was successful" }
    }

    /**
     * Currently empty, could be used to watch(and react) on certain zookeeper events
     */
    private class ZookeeperWatcher : Watcher {

        override fun process(event: WatchedEvent) {}
    }
}
