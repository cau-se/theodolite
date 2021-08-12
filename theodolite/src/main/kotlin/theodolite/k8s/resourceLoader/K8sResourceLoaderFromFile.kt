package theodolite.k8s.resourceLoader

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import theodolite.k8s.CustomResourceWrapper
import theodolite.util.YamlParserFromFile

/**
 * Used to load different Kubernetes resources.
 * Supports: Deployments, Services, ConfigMaps, and CustomResources.
 * @param client KubernetesClient used to deploy or remove.
 */
class K8sResourceLoaderFromFile(private val client: NamespacedKubernetesClient): AbstractK8sLoader(),
    K8sResourceLoader {

    /**
     * Parses a Service from a service yaml
     * @param resource of the yaml file
     * @return Service from fabric8
     */
    override fun loadService(resource: String): Service {
        return loadGenericResource(resource) { x: String -> client.services().load(x).get() }
    }


    /**
     * Parses a CustomResource from a yaml
     * @param path of the yaml file
     * @param context specific crd context for this custom resource
     * @return  CustomResourceWrapper from fabric8
     */
    override fun loadCustomResourceWrapper(resource: String, context: CustomResourceDefinitionContext): CustomResourceWrapper {
       return loadGenericResource(resource) {
           CustomResourceWrapper(
               YamlParserFromFile().parse(
                   resource,
                   HashMap<String, String>()::class.java
               )!!,
               context
           )
       }
   }

    /**
     * Parses a Deployment from a Deployment yaml
     * @param resource of the yaml file
     * @return Deployment from fabric8
     */
    override fun loadDeployment(resource: String): Deployment {
        return loadGenericResource(resource) { x: String -> client.apps().deployments().load(x).get() }
    }

    /**
     * Parses a ConfigMap from a ConfigMap yaml
     * @param resource of the yaml file
     * @return ConfigMap from fabric8
     */
    override fun loadConfigmap(resource: String): ConfigMap {
        return loadGenericResource(resource) { x: String -> client.configMaps().load(x).get() }
    }

    /**
     * Parses a StatefulSet from a StatefulSet yaml
     * @param resource of the yaml file
     * @return StatefulSet from fabric8
     */
    override fun loadStatefulSet(resource: String): KubernetesResource {
        return loadGenericResource(resource) { x: String -> client.apps().statefulSets().load(x).get() }

    }
}
