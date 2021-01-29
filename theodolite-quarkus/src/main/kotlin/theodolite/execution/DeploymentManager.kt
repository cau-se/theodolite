package theodolite.execution

import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.EnvVar
import io.fabric8.kubernetes.api.model.EnvVarSource
import io.fabric8.kubernetes.api.model.Quantity
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class DeploymentManager(client: NamespacedKubernetesClient) {
    var client: NamespacedKubernetesClient

    init {
        this.client = client
    }

    /**
     * Sets the ContainerEvironmentVariables, creates new if variable don t exist.
     * @param container - The Container
     * @param map - Map of k=Name,v =Value of EnviromentVariables
     */
    private fun setContainerEnv(container: Container, map: Map<String, String>) {
        map.forEach { k, v ->
            // filter for mathing name and set value
            val x = container.env.filter { envVar -> envVar.name == k }

            if (x.isEmpty()) {
                val newVar = EnvVar(k, v, EnvVarSource())
                container.env.add(newVar)
            } else {
                x.forEach {
                    it.value = v
                }
            }
        }
    }

    /**
     * Set the enviroment Variable for a container
     */
    fun setWorkloadEnv(workloadDeployment: Deployment, containerName: String, map: Map<String, String>) {
        workloadDeployment.spec.template.spec.containers.filter { it.name == containerName }
            .forEach { it: Container ->
                setContainerEnv(it, map)
            }
    }

    /**
     *  Change the RessourceLimit of a container (Usally SUT)
     */
    fun changeRessourceLimits(deployment: Deployment, ressource: String, containerName: String, limit: String) {
        deployment.spec.template.spec.containers.filter { it.name == containerName }.forEach {
            it.resources.limits.replace(ressource, Quantity(limit))
        }
    }

    /**
     * Change the image name of a container (SUT and the Worklaodgenerators)
     */
    fun setImageName(deployment: Deployment, containerName: String, image: String) {
        deployment.spec.template.spec.containers.filter { it.name == containerName }.forEach {
            it.image = image
        }
    }

    // TODO potential add exception handling
    fun deploy(deployment: Deployment) {
        client.apps().deployments().create(deployment)
    }

    // TODO potential add exception handling
    fun delete(deployment: Deployment) {
        client.apps().deployments().delete(deployment)
    }
}
