package theodolite.execution.operator

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResourceList
import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import java.lang.Thread.sleep

abstract class AbstractStateHandler<T,L,D>(
    private val client: KubernetesClient,
    private val crd: Class<T>,
    private val crdList: Class<L>
    ): StateHandler<T> where T : CustomResource<*, *>?, T: HasMetadata, T: Namespaced, L: KubernetesResourceList<T> {

    private val crdClient: MixedOperation<T, L,Resource<T>> =
        this.client.customResources(this.crd, this.crdList)

    @Synchronized
    override fun setState(resourceName: String, f: (T) -> T?) {
        this.crdClient
            .inNamespace(this.client.namespace)
            .list().items
            .filter { item -> item.metadata.name == resourceName }
            .map { customResource -> f(customResource) }
            .forEach { this.crdClient.updateStatus(it) }
       }

    @Synchronized
    override fun getState(resourceName: String, f: (T) -> String?): String? {
        return this.crdClient
            .inNamespace(this.client.namespace)
            .list().items
            .filter { item -> item.metadata.name == resourceName }
            .map { customResource -> f(customResource) }
            .firstOrNull()
    }

    @Synchronized
    override fun blockUntilStateIsSet(resourceName: String, desiredStatusString: String, f: (T) -> String?, maxTries: Int): Boolean {
        for (i in 0.rangeTo(maxTries)) {
            val currentStatus = getState(resourceName, f)
                if(currentStatus == desiredStatusString) {
                    return true
                }
            sleep(50)
        }
        return false
    }
}