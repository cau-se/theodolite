package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource

abstract class AbstractPatcher<T>(k8sResource: KubernetesResource): Patcher<T> {
}
