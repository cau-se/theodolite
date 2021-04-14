package theodolite.k8s

import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext

class K8sContextFactory {

    fun create(api: String, scope: String, group: String, plural: String  ) : CustomResourceDefinitionContext{
       return CustomResourceDefinitionContext.Builder()
            .withVersion(api)
            .withScope(scope)
            .withGroup(group)
            .withPlural(plural)
            .build()
    }
}