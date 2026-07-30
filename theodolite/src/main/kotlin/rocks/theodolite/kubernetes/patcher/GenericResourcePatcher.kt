package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.GenericKubernetesResource
import io.fabric8.kubernetes.api.model.HasMetadata

/**
 * Patches an arbitrary field in a [GenericKubernetesResource].
 *
 * @param path Path as List of Strings and Ints to the field to be patched.
 */
class GenericResourcePatcher(val path: List<Any>, val type: Type = Type.STRING) : AbstractStringPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {

        if (resource is GenericKubernetesResource) {
            val castedValue = when (type) {
                Type.STRING -> value
                Type.BOOLEAN -> value.toBoolean()
                Type.NUMBER -> value.toDouble()
                Type.INTEGER -> value.toInt()
            }
            var current: Any? = resource.additionalProperties
            for (segment in path.dropLast(1)) {
                current = if (segment is Int && current is MutableList<*> && current.size > segment) {
                    current[segment]
                } else if (segment is String && current is Map<*, *>) {
                    current[segment]
                } else {
                    throw IllegalArgumentException("Provided path is invalid")
                }
            }
            val segment = path.lastOrNull()
            if (segment == null) {
                throw IllegalArgumentException("Path must not be empty")
            } else if (segment is Int && current is MutableList<*> && current.size > segment) {
                @Suppress("UNCHECKED_CAST")
                (current as MutableList<Any?>)[segment] = castedValue
            } else if (segment is String && current is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                (current as MutableMap<String, Any>)[segment] = castedValue
            } else {
                throw IllegalArgumentException("Cannot set value for path")
            }
        }
        return resource
    }

    enum class Type(val value: String) {
        STRING("string"),
        BOOLEAN("boolean"),
        NUMBER("number"),
        INTEGER("integer");

        companion object {
            fun from(type: String): Type =
                values().find { it.value == type } ?: throw IllegalArgumentException("Requested Type does not exist")
        }
    }
}
