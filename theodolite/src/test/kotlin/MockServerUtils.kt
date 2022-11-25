import io.fabric8.kubernetes.api.model.APIResourceListBuilder
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext
import io.fabric8.kubernetes.client.server.mock.KubernetesServer

fun KubernetesServer.registerResource(context: ResourceDefinitionContext) {
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