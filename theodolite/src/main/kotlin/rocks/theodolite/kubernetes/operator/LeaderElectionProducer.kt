package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.extended.leaderelection.LeaderCallbacks
import io.javaoperatorsdk.operator.api.config.LeaderElectionConfiguration
import io.javaoperatorsdk.operator.api.config.LeaderElectionConfigurationBuilder
import io.quarkus.arc.Unremovable
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject
import jakarta.inject.Singleton
import mu.KotlinLogging
import rocks.theodolite.kubernetes.Configuration
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import java.time.Duration
import java.util.function.Consumer

private val logger = KotlinLogging.logger {}

/**
 * Configures JOSDK to elect a single active operator replica before reconciling resources.
 *
 * Leader election is provided as a [LeaderElectionConfiguration] CDI bean rather than through a
 * `ConfigurationServiceCustomizer`. Registering a customizer makes quarkus-operator-sdk wrap the
 * configuration in an `OverriddenConfigurationService` that does not delegate `getKubernetesClient()`
 * to the real configuration, so JOSDK falls back to the `ConfigurationService` default: a fresh
 * `KubernetesClientBuilder().build()` with a plain `KubernetesSerialization` that lacks the
 * [rocks.theodolite.kubernetes.util.KotlinLateinitModule]. Adding a finalizer via server-side apply
 * then serializes an Execution's uninitialized `lateinit` spec and fails. Providing the configuration
 * as a bean leaves the configuration unwrapped, so JOSDK keeps using the module-aware CDI client.
 *
 * The recorder reads the bean and activates it only for the profiles listed in
 * `quarkus.operator-sdk.activate-leader-election-for-profiles` (see `application.properties`); the
 * bean is otherwise ignored, e.g. in tests.
 */
@ApplicationScoped
class LeaderElectionProducer {

    @Inject
    lateinit var client: KubernetesClient

    @Inject
    lateinit var readiness: OperatorReadiness

    @Produces
    @Singleton
    @Unremovable
    fun leaderElectionConfiguration(): LeaderElectionConfiguration =
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
