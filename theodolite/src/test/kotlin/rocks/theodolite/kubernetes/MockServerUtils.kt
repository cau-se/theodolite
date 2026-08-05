package rocks.theodolite.kubernetes

import io.fabric8.kubernetes.api.model.APIResourceListBuilder
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer
import io.quarkus.test.kubernetes.client.KubernetesServer as QuarkusKubernetesServer

// Extension property for manually created mock servers (non-@KubernetesTestServer tests).
// The Quarkus KubernetesServer already exposes a `client` property via its getClient() getter.
val KubernetesMockServer.client: NamespacedKubernetesClient
    get() = createClient()

fun QuarkusKubernetesServer.registerResource(context: ResourceDefinitionContext) {
    val apiResourceList = APIResourceListBuilder()
        .addNewResource()
            .withName(context.plural)
            .withKind(context.kind)
            .withNamespaced(context.isNamespaceScoped)
        .endResource()
        .build()

    this
        .expect()
        .get()
        .withPath("/apis/${context.group}/${context.version}")
        .andReturn(200, apiResourceList)
        .always()
}