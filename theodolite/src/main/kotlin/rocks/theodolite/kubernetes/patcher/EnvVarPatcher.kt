package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet

/**
 * The EnvVarPatcher allows to modify the value of an environment variable
 *
 * @property container Name of the container to be patched.
 * @property variableName Name of the environment variable to be patched.
 */
class EnvVarPatcher(
        private val container: String,
        private val variableName: String
) : AbstractStringPatcher() {


    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        when (resource) {
            is Deployment -> {
                this.setEnvInPodSpec(
                        resource.spec.template.spec,
                        value
                )
            }
            is StatefulSet -> {
                this.setEnvInPodSpec(
                        resource.spec.template.spec,
                        value
                )
            }
            is Pod -> {
                this.setEnvInPodSpec(
                        resource.spec,
                        value
                )
            }
        }
        return resource
    }

    /**
     * Sets an environment variable for a container, creates new if variable does not exist.
     * @param container - The container for which to set the environment variable
     * @param value - Value to be set for the environment variable
     */
    private fun setEnvInContainer(container: Container, value: String) {
        val envVars = container.env.filter { it.name == this.variableName }
        if (envVars.isEmpty()) {
            container.env.add(EnvVar().also {
                it.name = this.variableName
                it.value = value
            })
        } else {
            envVars.forEach {
                it.value = value
            }
        }
    }

    /**
     * Set the environment variable for a container in a [PodSpec].
     */
    private fun setEnvInPodSpec(podSpec: PodSpec, value: String) {
        podSpec.containers
                .filter { it.name == this.container }
                .forEach { setEnvInContainer(it, value) }
    }
}
