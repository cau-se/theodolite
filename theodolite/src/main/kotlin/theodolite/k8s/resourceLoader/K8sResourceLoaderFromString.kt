package theodolite.k8s.resourceLoader

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import theodolite.k8s.CustomResourceWrapper
import theodolite.util.YamlParserFromString
import java.io.ByteArrayInputStream
import java.io.InputStream

class K8sResourceLoaderFromString(private val client: NamespacedKubernetesClient): AbstractK8sLoader(),
    K8sResourceLoader {

    override fun loadService(resource: String): Service {
        return loadAnyResource(resource) { stream -> client.services().load(stream).get() }
    }

    override fun loadDeployment(resource: String): Deployment {
        return loadAnyResource(resource) { stream -> client.apps().deployments().load(stream).get() }
    }

    override fun loadConfigmap(resource: String): ConfigMap {
        return loadAnyResource(resource) { stream -> client.configMaps().load(stream).get() }
    }

    override fun loadStatefulSet(resource: String): StatefulSet {
        return loadAnyResource(resource) { stream -> client.apps().statefulSets().load(stream).get() }
    }

    private fun <T : KubernetesResource> loadAnyResource(resource: String, f: (InputStream) -> T): T {
        return loadGenericResource(resource) { f(ByteArrayInputStream(it.encodeToByteArray())) }
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