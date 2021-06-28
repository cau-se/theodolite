package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Representation of the load dimensions for a execution of theodolite.
 *
 * @param number the value of this [LoadDimension]
 * @param type [PatcherDefinition] of this [LoadDimension]
 */
@RegisterForReflection
data class LoadDimension(private val number: Int, private val type: List<PatcherDefinition>) {
    /**
     * @return the value of this load dimension.
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
