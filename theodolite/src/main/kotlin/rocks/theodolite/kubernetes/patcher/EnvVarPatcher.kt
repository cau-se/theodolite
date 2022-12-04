package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.EnvVar
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.apps.Deployment

/**
 * The EnvVarPatcher allows to modify the value of an environment variable
 *
 * @property container Container to be patched.
 * @property variableName Name of the environment variable to be patched.
 */
class EnvVarPatcher(
    private val container: String,
    private val variableName: String
) : AbstractStringPatcher() {


    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        if (resource is Deployment) {
            this.setEnv(
                resource, this.container,
                mapOf(this.variableName to value)
            )
            return resource
        }
        return resource
    }

    /**
     * Sets the ContainerEnvironmentVariables, creates new if variable does not exist.
     * @param container - The Container
     * @param map - Map of k=Name,v =Value of EnvironmentVariables
     */
    private fun setContainerEnv(container: Container, map: Map<String, String>) {
        map.forEach { (k, v) ->
            // filter for matching name and set value
            val x = container.env.filter { envVar -> envVar.name == k }

            if (x.isEmpty()) {
                val newVar = EnvVar()
                newVar.name = k
                newVar.value = v
                container.env.add(newVar)
            } else {
                x.forEach {
                    it.value = v
                }
            }
        }
    }

    /**
     * Set the environment Variable for a container
     */
    private fun setEnv(workloadDeployment: Deployment, containerName: String, map: Map<String, String>) {
        workloadDeployment.spec.template.spec.containers.filter { it.name == containerName }
            .forEach { setContainerEnv(it, map) }
    }
}
