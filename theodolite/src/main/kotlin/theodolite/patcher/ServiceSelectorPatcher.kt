package theodolite.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.utils.Serialization

class ServiceSelectorPatcher(
    private var variableName: String
    ) : AbstractPatcher() {

    override fun patchSingeResource(resource: HasMetadata, value: String): HasMetadata {
        if (resource is Service) {
            if (resource.spec.selector == null) {
                resource.spec.selector = mutableMapOf()
            }
            resource.spec.selector[this.variableName] = value
        }
        return resource
    }
    }