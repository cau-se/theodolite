package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Representation of the resources for an execution of Theodolite.
 */
@RegisterForReflection
data class Resource(private val number: Int, private val type: List<PatcherDefinition>) {

    /**
     * @return the value of this resource.
     */
    fun get(): Int {
        return this.number
    }

    /**
     * @return the list of [PatcherDefinition]
     */
    fun getType(): List<PatcherDefinition> {
        return this.type
    }
}
