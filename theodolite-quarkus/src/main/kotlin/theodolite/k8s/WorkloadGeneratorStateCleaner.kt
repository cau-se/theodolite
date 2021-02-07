package theodolite.k8s

import mu.KotlinLogging
import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooKeeper
import java.time.Duration

private val logger = KotlinLogging.logger {}

/**
 * Resets the workloadgenerator states in zookeper (and potentially watches for Zookeper events)
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
     * Deletes a Zookeeper node and its children with the corresponding path.
     */
    fun deleteAll() {

        while (true) {
            var children = emptyList<String>();
            try {
                children = zookeeperClient.getChildren(this.path, true)
            } catch (e: KeeperException.NoNodeException) {
                break;
            }
            // delete all children nodes
            for (s: String in children) {
                try {
                    zookeeperClient.delete("${this.path}/$s", -1)
                } catch (ex: Exception) {
                    logger.info { "$ex" }
                }
            }

            // delete main node
            try {
                zookeeperClient.delete(this.path, -1)
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
        logger.info { "ZooKeeper reset was successful" }
    }

    /**
     * Currently empty, could be used to watch(and react) on certain zookeeper events
     */
    private class ZookeeperWatcher : Watcher {

        override fun process(event: WatchedEvent) {}
    }
}
