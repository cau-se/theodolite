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
     * @param path of the yaml file
     * @return Service from fabric8
     */
    override fun loadService(path: String): Service {
        return loadGenericResource(path) { x: String -> client.services().load(x).get() }
    }


    /**
     * Parses a CustomResource from a yaml
     * @param path of the yaml file
     * @param context specific crd context for this custom resource
     * @return  CustomResourceWrapper from fabric8
     */
    override fun loadCustomResourceWrapper(path: String, context: CustomResourceDefinitionContext): CustomResourceWrapper {
       return loadGenericResource(path) {
           CustomResourceWrapper(
               YamlParserFromFile().parse(
                   path,
                   HashMap<String, String>()::class.java
               )!!,
               context
           )
       }
   }

    /**
     * Parses a Deployment from a Deployment yaml
     * @param path of the yaml file
     * @return Deployment from fabric8
     */
    override fun loadDeployment(path: String): Deployment {
        return loadGenericResource(path) { x: String -> client.apps().deployments().load(x).get() }
    }

    /**
     * Parses a ConfigMap from a ConfigMap yaml
     * @param path of the yaml file
     * @return ConfigMap from fabric8
     */
    override fun loadConfigmap(path: String): ConfigMap {
        return loadGenericResource(path) { x: String -> client.configMaps().load(x).get() }
    }

    /**
     * Parses a StatefulSet from a StatefulSet yaml
     * @param path of the yaml file
     * @return StatefulSet from fabric8
     */
    override fun loadStatefulSet(path: String): KubernetesResource {
        return loadGenericResource(path) { x: String -> client.apps().statefulSets().load(x).get() }

    }
}
