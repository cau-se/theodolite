package theodolite.execution.operator

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResourceList
import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import mu.KotlinLogging
import java.lang.Thread.sleep
private val logger = KotlinLogging.logger {}

abstract class AbstractStateHandler<T, L, D>(
    private val client: NamespacedKubernetesClient,
    private val crd: Class<T>,
    private val crdList: Class<L>
) : StateHandler<T> where T : CustomResource<*, *>?, T : HasMetadata, T : Namespaced, L : KubernetesResourceList<T> {

    private val crdClient: MixedOperation<T, L, Resource<T>> =
        this.client.customResources(this.crd, this.crdList)

    @Synchronized
    override fun setState(resourceName: String, f: (T) -> T?) {
        try {
            this.crdClient
                .list().items
                .filter { it.metadata.name == resourceName }
                .map { customResource -> f(customResource) }
                .forEach { this.crdClient.updateStatus(it) }
        } catch (e: KubernetesClientException) {
            logger.warn { "Status cannot be set for resource $resourceName" }
        }
    }

    @Synchronized
    override fun getState(resourceName: String, f: (T) -> String?): String? {
        return this.crdClient
            .list().items
            .filter { it.metadata.name == resourceName }
            .map { customResource -> f(customResource) }
            .firstOrNull()
    }

    @Synchronized
    override fun blockUntilStateIsSet(
        resourceName: String,
        desiredStatusString: String,
        f: (T) -> String?,
        maxRetries: Int
    ): Boolean {
        for (i in 0.rangeTo(maxRetries)) {
            val currentStatus = getState(resourceName, f)
            if (currentStatus == desiredStatusString) {
                return true
            }
            sleep(50)
        }
        return false
    }
}