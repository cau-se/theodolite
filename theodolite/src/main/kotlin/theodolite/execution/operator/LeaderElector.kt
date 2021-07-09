package theodolite.execution.operator

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.extended.leaderelection.LeaderCallbacks
import io.fabric8.kubernetes.client.extended.leaderelection.LeaderElectionConfigBuilder
import io.fabric8.kubernetes.client.extended.leaderelection.resourcelock.LeaseLock
import mu.KotlinLogging
import java.time.Duration
import java.util.*
import kotlin.reflect.KFunction0

private val logger = KotlinLogging.logger {}

class LeaderElector(
    val client:  NamespacedKubernetesClient,
    val name: String
    ) {

    fun getLeadership(leader: KFunction0<Unit>) {
        val lockIdentity: String = UUID.randomUUID().toString()
            DefaultKubernetesClient().use { kc ->
                kc.leaderElector()
                .withConfig(
                    LeaderElectionConfigBuilder()
                        .withName("Theodolite")
                        .withLeaseDuration(Duration.ofSeconds(15L))
                        .withLock(LeaseLock(client.namespace, name, lockIdentity))
                        .withRenewDeadline(Duration.ofSeconds(10L))
                        .withRetryPeriod(Duration.ofSeconds(2L))
                        .withLeaderCallbacks(LeaderCallbacks(
                            { Thread{leader()}.start() },
                            { logger.info { "STOPPED LEADERSHIP" } }
                        ) { newLeader: String? ->
                           logger.info { "New leader elected $newLeader" }
                        })
                        .build()
                )
                .build().run()
        }
    }

}