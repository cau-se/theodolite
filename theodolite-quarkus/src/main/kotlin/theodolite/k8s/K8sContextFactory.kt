package theodolite.k8s

import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext

/**
 * Factory for CustomResourceDefinitionContext
 *
 * @see CustomResourceDefinitionContext
 */
class K8sContextFactory {

    /**
     * Create a CustomResourceDefinitionContext.
     *
     * @param api The K8s API version
     * @param scope The scope of the CRD
     * @param group The group of the CRD
     * @param plural The plural name (kind) of the CRD
     *
     * @return a new CustomResourceDefinitionContext
     *
     * @see CustomResourceDefinitionContext
     */
    fun create(api: String, scope: String, group: String, plural: String): CustomResourceDefinitionContext {
        return CustomResourceDefinitionContext.Builder()
            .withVersion(api)
            .withScope(scope)
            .withGroup(group)
            .withPlural(plural)
            .build()
    }
}
