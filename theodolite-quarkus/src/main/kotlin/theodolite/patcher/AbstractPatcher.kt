package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource

abstract class AbstractPatcher(
    k8sResource: KubernetesResource,
    container: String? = null,
    variableName: String? = null
) : Patcher
