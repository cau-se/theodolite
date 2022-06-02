package theodolite.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.utils.Serialization

/**
 * The Image patcher allows to change the image of a container.
 *
 * @param container Container to be patched.
 */
class ImagePatcher(
    private val container: String) :
    AbstractPatcher() {

    override fun patch(resources: List<HasMetadata>, value: String) : List<HasMetadata> {
        return resources
            .map { Serialization.clone(it) }
            .map { patchSingleResource(it, value as kotlin.String) }
    }

    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        if (resource is Deployment) {
            (resource).spec.template.spec.containers.filter { it.name == container }.forEach {
                it.image = value
            }
            return resource
        } else if (resource is StatefulSet) {
            (resource).spec.template.spec.containers.filter { it.name == container }.forEach {
                it.image = value
            }
            return resource
        }
        return resource
    }
}
