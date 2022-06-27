package rocks.theodolite.kubernetes

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
@JsonDeserialize
interface ResourceSet: KubernetesResource {

    fun getResourceSet(client: NamespacedKubernetesClient): Collection<Pair<String, KubernetesResource>>
}