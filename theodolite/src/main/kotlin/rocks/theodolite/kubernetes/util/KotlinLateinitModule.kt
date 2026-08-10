package rocks.theodolite.kubernetes.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier

/**
 * Jackson module that handles Kotlin `lateinit` properties that have not been initialized.
 * Without this module, serializing an object with an uninitialized `lateinit` property throws
 * [UninitializedPropertyAccessException], which propagates wrapped inside a
 * [com.fasterxml.jackson.databind.JsonMappingException]. With this module registered, uninitialized
 * properties are serialized as `null` instead.
 */
class KotlinLateinitModule : SimpleModule() {

    init {
        setSerializerModifier(object : BeanSerializerModifier() {
            override fun changeProperties(
                config: SerializationConfig,
                beanDesc: BeanDescription,
                beanProperties: MutableList<BeanPropertyWriter>
            ) = beanProperties.map { LateinitSafeBeanPropertyWriter(it) }.toMutableList()
        })
    }
}

private class LateinitSafeBeanPropertyWriter(base: BeanPropertyWriter) : BeanPropertyWriter(base) {
    override fun serializeAsField(bean: Any?, gen: JsonGenerator, prov: SerializerProvider) {
        try {
            super.serializeAsField(bean, gen, prov)
        } catch (e: Exception) {
            if (e.causedByUninitializedProperty()) {
                gen.writeNullField(name)
            } else {
                throw e
            }
        }
    }
}

private fun Throwable.causedByUninitializedProperty(): Boolean {
    var cause: Throwable? = this
    while (cause != null) {
        if (cause is UninitializedPropertyAccessException) return true
        cause = cause.cause
    }
    return false
}
