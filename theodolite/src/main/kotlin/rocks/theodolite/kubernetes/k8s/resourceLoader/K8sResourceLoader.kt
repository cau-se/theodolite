package rocks.theodolite.kubernetes.k8s.resourceLoader

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext

interface K8sResourceLoader {
    fun loadDeployment(resource: String): KubernetesResource
    fun loadService(resource: String): KubernetesResource
    fun loadStatefulSet(resource: String): KubernetesResource
    fun loadExecution(resource: String): KubernetesResource
    fun loadBenchmark(resource: String): KubernetesResource
    fun loadConfigmap(resource: String): KubernetesResource
    fun loadServiceMonitor(resource: String): KubernetesResource
    fun loadPodMonitor(resource: String): KubernetesResource
    fun loadCustomResourceWrapper(resource: String, context: CustomResourceDefinitionContext): KubernetesResource
}