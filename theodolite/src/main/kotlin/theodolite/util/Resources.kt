package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Representation of the resources for an execution of Theodolite.
 */
@RegisterForReflection
data class Resources(private val resourceList: List<Int>, private val type: List<PatcherDefinition>) {

    /**
     * @return the list of resources.
     */
    fun get(): List<Int> {
        return this.resourceList
    }

    /**
     * @return the resource at a given Index.
     */
    fun getIndex(i: Int): Int {
        return this.resourceList[i]
    }

    /**
     * @return the list of [PatcherDefinition]
     */
    fun getType(): List<PatcherDefinition> {
        return this.type
    }
}
