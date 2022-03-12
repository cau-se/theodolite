package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResourceList
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import mu.KotlinLogging
import java.lang.Thread.sleep
private val logger = KotlinLogging.logger {}

private const val MAX_RETRIES: Int = 5

abstract class AbstractStateHandler<S : HasMetadata>(
    private val client: NamespacedKubernetesClient,
    private val crd: Class<S>
) {

    private val crdClient: MixedOperation<S, KubernetesResourceList<S>, Resource<S>> = this.client.resources(this.crd)

    @Synchronized
    fun setState(resourceName: String, setter: (S) -> S?) {
        try {
            val resource = this.crdClient.withName(resourceName).get()
            if (resource != null) {
                val resourcePatched = setter(resource)
                this.crdClient.patchStatus(resourcePatched)
            }
        } catch (e: KubernetesClientException) {
            logger.warn(e) { "Status cannot be set for resource $resourceName." }
        }
    }

    @Synchronized
    fun getState(resourceName: String, f: (S) -> String?): String? {
        return this.crdClient
            .list().items
            .filter { it.metadata.name == resourceName }
            .map { customResource -> f(customResource) }
            .firstOrNull()
    }

    @Synchronized
    fun blockUntilStateIsSet(
        resourceName: String,
        desiredStatusString: String,
        f: (S) -> String?,
        maxRetries: Int = MAX_RETRIES
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