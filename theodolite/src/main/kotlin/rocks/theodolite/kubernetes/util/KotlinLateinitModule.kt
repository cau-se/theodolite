package rocks.theodolite.kubernetes.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import java.lang.reflect.InvocationTargetException

/**
 * Jackson module that handles Kotlin `lateinit` properties that have not been initialized.
 *
 * Without this module, serializing an object with an uninitialized `lateinit` property throws
 * [UninitializedPropertyAccessException], which Jackson propagates wrapped in a
 * [com.fasterxml.jackson.databind.JsonMappingException]. This breaks the Java Operator SDK's
 * server-side apply calls (e.g. when adding a finalizer), which serialize a custom resource that
 * only has its metadata set. With this module registered, such properties are omitted instead,
 * which keeps the fields non-nullable in the Kotlin type system.
 *
 * Properties are omitted rather than written as `null`, because an uninitialized `lateinit`
 * property is unset rather than null. Writing `null` would also bypass the NON_NULL inclusion the
 * Kubernetes client configures, and the API server rejects a `null` for a property that the CRD
 * schema declares with a type.
 *
 * Must be registered on the object mapper used by the Kubernetes client, see
 * [TheodoliteKubernetesClientObjectMapperCustomizer].
 */
class KotlinLateinitModule : SimpleModule() {

    init {
        setSerializerModifier(object : BeanSerializerModifier() {
            override fun changeProperties(
                config: SerializationConfig,
                beanDesc: BeanDescription,
                beanProperties: MutableList<BeanPropertyWriter>
            ) = beanProperties.map { LateinitSafeBeanPropertyWriter(it) as BeanPropertyWriter }.toMutableList()
        })
    }
}

/**
 * Delegating [BeanPropertyWriter] that omits uninitialized `lateinit` properties.
 *
 * Serialization is delegated to the [wrapped] writer instead of calling `super`, so that behavior
 * added by other modules (such as the Kubernetes client's own property writer delegates) is
 * preserved.
 */
private class LateinitSafeBeanPropertyWriter(
    private val wrapped: BeanPropertyWriter
) : BeanPropertyWriter(wrapped) {

    override fun serializeAsField(bean: Any?, gen: JsonGenerator, prov: SerializerProvider) {
        try {
            this.wrapped.serializeAsField(bean, gen, prov)
        } catch (e: InvocationTargetException) {
            // Thrown if Jackson reads the property via its getter, which is the usual case.
            if (e.targetException !is UninitializedPropertyAccessException) {
                throw e
            }
        } catch (_: UninitializedPropertyAccessException) {
            // Thrown if Jackson reads the property via its backing field.
        }
    }

    override fun assignSerializer(ser: JsonSerializer<Any>) = this.wrapped.assignSerializer(ser)

    override fun assignNullSerializer(ser: JsonSerializer<Any>) = this.wrapped.assignNullSerializer(ser)

    override fun assignTypeSerializer(ser: TypeSerializer) = this.wrapped.assignTypeSerializer(ser)
}
