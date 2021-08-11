package theodolite.benchmark

import io.fabric8.kubernetes.api.model.KubernetesResource

interface ResourceSet {

    fun getResourceSet(): List<Pair<String, KubernetesResource>>
}