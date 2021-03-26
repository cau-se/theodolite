package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class Resource(private val number: Int, private val type: List<PatcherDefinition>) {

    fun get(): Int {
        return this.number
    }

    fun getType(): List<PatcherDefinition> {
        return this.type
    }
}
