package theodolite.execution.operator

import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.client.CustomResourceDoneable
import io.fabric8.kubernetes.client.CustomResourceList
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import java.lang.Thread.sleep

abstract class AbstractStateHandler<T,L,D>(
    private val context: CustomResourceDefinitionContext,
    private val client: KubernetesClient,
    private val crd: Class<T>,
    private val crdList: Class<L>,
    private val donableCRD: Class<D>
    ): StateHandler where T : CustomResource, T: Namespaced, L: CustomResourceList<T>, D: CustomResourceDoneable<T> {

    private val crdClient: MixedOperation<T, L, D, Resource<T, D>> =
        this.client.customResources(this.context, this.crd, this.crdList, this.donableCRD)

    @Synchronized
    override fun setState(resourceName: String, f: (CustomResource) -> CustomResource?) {
        this.crdClient
            .inNamespace(this.client.namespace)
            .list().items
            .filter { item -> item.metadata.name == resourceName }
            .map { customResource -> f(customResource) }
            .forEach { this.crdClient.updateStatus(it as T) }
       }

    @Synchronized
    override fun getState(resourceName: String, f: (CustomResource) -> String?): String? {
        return this.crdClient
            .inNamespace(this.client.namespace)
            .list().items
            .filter { item -> item.metadata.name == resourceName }
            .map { customResource -> f(customResource) }
            .firstOrNull()
    }

    @Synchronized
    override fun blockUntilStateIsSet(resourceName: String, desiredStatusString: String, f: (CustomResource) -> String?, maxTries: Int): Boolean {
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