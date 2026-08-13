package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet

/**
 * The Image patcher allows to change the image of a container.
 *
 * @param container Container to be patched.
 */
class ImagePatcher(private val container: String, private val pullPolicy: String? = null) : AbstractStringPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        when (resource) {
            is Deployment -> {
                resource.spec.template.spec.containers.filter { it.name == container }.forEach {
                    it.image = value
                    if (pullPolicy != null) {
                        it.imagePullPolicy = pullPolicy
                    }
                }
            }
            is StatefulSet -> {
                resource.spec.template.spec.containers.filter { it.name == container }.forEach {
                    it.image = value
                    if (pullPolicy != null) {
                        it.imagePullPolicy = pullPolicy
                    }
                }
            }
            is Pod -> {
                resource.spec.containers.filter { it.name == container }.forEach {
                    it.image = value
                    if (pullPolicy != null) {
                        it.imagePullPolicy = pullPolicy
                    }
                }
            }
        }
        return resource
    }

}
