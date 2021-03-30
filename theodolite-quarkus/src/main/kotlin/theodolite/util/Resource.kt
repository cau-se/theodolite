package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Representation of the resources for a execution of theodolite.
 */
@RegisterForReflection
data class Resource(private val number: Int, private val type: List<PatcherDefinition>) {

    /**
     * @return the value of this load dimension.
     *
     * @see LoadDimension
     */
    fun get(): Int {
        return this.number
    }

    /**
     * @return the list of patcher definitions.
     *
     * @see PatcherDefinition
     */
    fun getType(): List<PatcherDefinition> {
        return this.type
    }
}
