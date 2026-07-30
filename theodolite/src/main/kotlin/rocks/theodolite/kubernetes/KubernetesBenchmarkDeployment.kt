package rocks.theodolite.kubernetes

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import mu.KotlinLogging
import theodolite.benchmark.RolloutManager
import java.time.Duration

private val logger = KotlinLogging.logger {}

/**
 * Organizes the deployment of benchmarks in Kubernetes.
 *
 * @param namespace to operate in.
 * @param resources List of [KubernetesResource] that are managed.
 */
@RegisterForReflection
class KubernetesBenchmarkDeployment(
    private val sutBeforeActions: List<Action>,
    private val sutAfterActions: List<Action>,
    private val loadGenBeforeActions: List<Action>,
    private val loadGenAfterActions: List<Action>,
    private val rolloutMode: Boolean,
    val appResources: List<HasMetadata>,
    val loadGenResources: List<HasMetadata>,
    private val loadGenerationDelay: Long,
    private val afterTeardownDelay: Long,
    private val client: NamespacedKubernetesClient
) : BenchmarkDeployment {
    private val kubernetesManager = K8sManager(client)
    private val LAG_EXPORTER_POD_LABEL_NAME = "app.kubernetes.io/name"
    private val LAG_EXPORTER_POD_LABEL_VALUE = "kafka-exporter"



    /**
     * Setup a [KubernetesBenchmark] using the [K8sManager]:
     *  - Create the needed topics.
     *  - Deploy the needed [KubernetesResource]s (deployment order: SUT resources, loadgenerator resources;
     *    Order of files within a configmap follows the `files` list when specified, otherwise the ConfigMap data order).
     */
    override fun setup() {
        val rolloutManager = RolloutManager(rolloutMode, client)

        sutBeforeActions.forEach { it.exec(client = client) }
        rolloutManager.rollout(appResources)
        logger.info { "Wait ${this.loadGenerationDelay} seconds before starting the load generator." }
        Thread.sleep(Duration.ofSeconds(this.loadGenerationDelay).toMillis())
        loadGenBeforeActions.forEach { it.exec(client = client) }
        rolloutManager.rollout(loadGenResources)
    }

    /**
     * Tears down a [KubernetesBenchmark]:
     *  - Reset the Kafka Lag Exporter.
     *  - Remove the used topics.
     *  - Remove the [KubernetesResource]s (removal order: loadgenerator resources, SUT resources;
     *    Order of files within a ConfigMap follows the reverse of the `files` list when specified, otherwise the reverse of the ConfigMap data order).
     */
    override fun teardown() {
        val podCleaner = ResourceByLabelHandler(client)
        loadGenResources.reversed().forEach {
            logger.info { "Deleting ${it.kind} '${it.metadata.name}'." }
            kubernetesManager.remove(it, true)
        }
        loadGenAfterActions.forEach { it.exec(client = client) }
        appResources.reversed().forEach {
            logger.info { "Deleting ${it.kind} '${it.metadata.name}'." }
            kubernetesManager.remove(it, true)
        }
        sutAfterActions.forEach { it.exec(client = client) }

        // TODO This does NOT work because of the listOf(..) (should be (loadGenResources + appResources)), but might be removed anyways
        listOf(loadGenResources, appResources)
            .forEach {
                if (it is Deployment) {
                    podCleaner.blockUntilPodsDeleted(it.spec.selector.matchLabels)
                } else if (it is StatefulSet) {
                    podCleaner.blockUntilPodsDeleted(it.spec.selector.matchLabels)
                }
            }

        podCleaner.removePods(
            labelName = LAG_EXPORTER_POD_LABEL_NAME,
            labelValue = LAG_EXPORTER_POD_LABEL_VALUE
        )
        logger.info { "Teardown complete. Wait $afterTeardownDelay seconds to let everything cool down." }
        Thread.sleep(Duration.ofSeconds(afterTeardownDelay).toMillis())
    }
}