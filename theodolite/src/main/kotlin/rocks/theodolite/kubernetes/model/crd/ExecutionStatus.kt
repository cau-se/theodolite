package rocks.theodolite.kubernetes.model.crd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.fabric8.kubernetes.api.model.Duration
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.MicroTime
import io.fabric8.kubernetes.api.model.Namespaced
import java.time.Clock
import java.time.Instant
import java.time.Duration as JavaDuration


@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
class ExecutionStatus(
    private val clock: Clock = Clock.systemUTC()
) : KubernetesResource, Namespaced {

    var executionState: ExecutionState = ExecutionState.NO_STATE

    var startTime: MicroTime? = null

    var completionTime: MicroTime? = null

    @get:JsonSerialize(using = DurationSerializer::class)
    val executionDuration: Duration?
        get() {
            val startTime = this.startTime?.toInstant()
            val completionTime = this.completionTime?.toInstant() ?: clock.instant()!!
            return startTime?.let {Duration(JavaDuration.between(it, completionTime)) }
        }

    private fun MicroTime.toInstant(): Instant {
        return Instant.parse(this.time)
    }

    class DurationSerializer : JsonSerializer<Duration?>() {

        override fun serialize(duration: Duration?, generator: JsonGenerator, serProvider: SerializerProvider) {
            generator.writeObject(duration?.duration?.toK8sString())
        }

        private fun JavaDuration.toK8sString(): String {
            return when {
                this <= JavaDuration.ofMinutes(2)  -> "${this.toSeconds()}s"
                this < JavaDuration.ofMinutes(99) -> "${this.toMinutes()}m"
                this < JavaDuration.ofHours(99) -> "${this.toHours()}h"
                else -> "${this.toDays()}d + ${this.minusDays(this.toDays()).toHours()}h"
            }
        }

    }
}