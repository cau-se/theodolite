package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.client.utils.Serialization

/**
 * A Patcher is able to modify values of a Kubernetes resource, see [Patcher].
 */
abstract class AbstractStringPatcher(
    val prefix: String = "",
    val suffix: String = ""
) : Patcher {

    final override fun patch(resources: List<HasMetadata>, value: String) : List<HasMetadata> {
        return resources
            .map { Serialization.clone(it)}
            .map { patchSingleResource(it, prefix + value) }
    }

    abstract fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata

}
