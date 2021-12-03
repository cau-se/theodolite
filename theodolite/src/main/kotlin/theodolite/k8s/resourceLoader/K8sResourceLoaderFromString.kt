package theodolite.k8s.resourceLoader

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import theodolite.k8s.CustomResourceWrapper
import theodolite.util.YamlParserFromString
import java.io.ByteArrayInputStream

class K8sResourceLoaderFromString(private val client: NamespacedKubernetesClient): AbstractK8sLoader(),
    K8sResourceLoader {

    @OptIn(ExperimentalStdlibApi::class)
    override fun loadService(resource: String): KubernetesResource {
        return loadGenericResource(resource) { x: String ->
            val stream = ByteArrayInputStream(x.encodeToByteArray())
            client.services().load(stream).get() }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun loadDeployment(resource: String): Deployment {
        return loadGenericResource(resource) { x: String ->
            val stream = ByteArrayInputStream(x.encodeToByteArray())
            client.apps().deployments().load(stream).get() }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun loadConfigmap(resource: String): ConfigMap {
        return loadGenericResource(resource) { x: String ->
            val stream = ByteArrayInputStream(x.encodeToByteArray())
            client.configMaps().load(stream).get() }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun loadStatefulSet(resource: String): KubernetesResource {
        return loadGenericResource(resource) { x: String ->
            val stream = ByteArrayInputStream(x.encodeToByteArray())
            client.apps().statefulSets().load(stream).get() }
    }

    /**
     * Parses a CustomResource from a yaml
     * @param resource of the yaml file
     * @param context specific crd context for this custom resource
     * @return  CustomResourceWrapper from fabric8
     */
    override fun loadCustomResourceWrapper(resource: String, context: CustomResourceDefinitionContext): CustomResourceWrapper {
        return loadGenericResource(resource) {
            CustomResourceWrapper(
                YamlParserFromString().parse(
                    resource,
                    HashMap<String, String>()::class.java
                )!!,
                context
            )
        }
    }
}