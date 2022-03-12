package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.api.model.EventBuilder
import io.fabric8.kubernetes.api.model.EventSource
import io.fabric8.kubernetes.api.model.ObjectReference
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging
import rocks.theodolite.kubernetes.util.Configuration
import java.time.Instant
import java.util.*
import kotlin.NoSuchElementException
private val logger = KotlinLogging.logger {}

class EventCreator {
    val client: NamespacedKubernetesClient = DefaultKubernetesClient().inNamespace(Configuration.NAMESPACE)

    fun createEvent(executionName: String, type: String, message: String, reason: String) {
        val uuid = UUID.randomUUID().toString()
        try {
            val objectRef = buildObjectReference(executionName)
            val event = EventBuilder()
                .withNewMetadata()
                .withName(uuid)
                .endMetadata()
                .withMessage(message)
                .withReason(reason)
                .withType(type)
                .withFirstTimestamp(Instant.now().toString()) // TODO change datetime format
                .build()

            val source =  EventSource()
            source.component = Configuration.COMPONENT_NAME
            event.source = source

            event.involvedObject = objectRef
            client.v1().events().inNamespace(Configuration.NAMESPACE).createOrReplace(event)
        } catch (e: NoSuchElementException) {
                logger.warn {"Could not create event: type: $type, message: $message, reason: $reason, no corresponding execution found."}
        }
    }

    private fun buildObjectReference(executionName: String): ObjectReference {
        val exec = TheodoliteOperator()
            .getExecutionClient(client = client)
            .list()
            .items
            .first{it.metadata.name == executionName}

        val objectRef = ObjectReference()
        objectRef.apiVersion = exec.apiVersion
        objectRef.kind = exec.kind
        objectRef.uid = exec.metadata.uid
        objectRef.name = exec.metadata.name
        objectRef.namespace = exec.metadata.namespace
        objectRef.resourceVersion = exec.metadata.resourceVersion

        return objectRef
    }
}