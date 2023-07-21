package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.client.utils.Serialization

/**
 * A Patcher is able to modify values of a Kubernetes resource, see [Patcher].
 */
abstract class AbstractIntPatcher : Patcher {

    final override fun patch(resources: List<HasMetadata>, value: String) : List<HasMetadata> {
        return resources
            .map { Serialization.clone(it)}
            .map { patchSingleResource(it, value.toInt()) }
    }

    abstract fun patchSingleResource(resource: HasMetadata, value: Int): HasMetadata

}
