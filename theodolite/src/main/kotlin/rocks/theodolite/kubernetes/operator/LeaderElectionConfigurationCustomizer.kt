package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.extended.leaderelection.LeaderCallbacks
import io.javaoperatorsdk.operator.api.config.LeaderElectionConfigurationBuilder
import io.quarkiverse.operatorsdk.runtime.api.ConfigurationServiceCustomizer
import io.quarkus.runtime.LaunchMode
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import mu.KotlinLogging
import rocks.theodolite.kubernetes.Configuration
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import java.time.Duration
import java.util.function.Consumer

private val logger = KotlinLogging.logger {}

/** Configures JOSDK to elect a single active operator replica before reconciling resources. */
@ApplicationScoped
class LeaderElectionConfigurationCustomizer : ConfigurationServiceCustomizer {

    @Inject
    lateinit var client: KubernetesClient

    @Inject
    lateinit var readiness: OperatorReadiness

    @Inject
    lateinit var launchMode: LaunchMode

    override fun overrider(): Consumer<io.javaoperatorsdk.operator.api.config.ConfigurationServiceOverrider> {
        return Consumer { overrider ->
            if (launchMode != LaunchMode.TEST) {
                overrider.withLeaderElectionConfiguration(
                    LeaderElectionConfigurationBuilder
                        .aLeaderElectionConfiguration(Configuration.COMPONENT_NAME)
                        .withLeaseNamespace(Configuration.NAMESPACE)
                        .withLeaseDuration(Duration.ofSeconds(15))
                        .withRenewDeadline(Duration.ofSeconds(10))
                        .withRetryPeriod(Duration.ofSeconds(2))
                        .withLeaderCallbacks(
                            LeaderCallbacks(
                                Runnable(::clearClusterState),
                                Runnable(readiness::close),
                                Consumer { leader -> logger.info { "New leader elected: $leader" } }
                            )
                        )
                        .build()
                )
            }
        }
    }

    private fun clearClusterState() {
        logger.info { "Becoming the leading operator. Use namespace '${Configuration.NAMESPACE}'." }
        val namespacedClient = client
            .adapt(NamespacedKubernetesClient::class.java)
            .inNamespace(Configuration.NAMESPACE)
        ClusterSetup(
            executionCRDClient = namespacedClient.resources(ExecutionCRD::class.java),
            benchmarkCRDClient = namespacedClient.resources(BenchmarkCRD::class.java),
            client = namespacedClient
        ).clearClusterState()
        readiness.open()
    }
}