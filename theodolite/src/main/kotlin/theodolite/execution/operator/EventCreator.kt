package theodolite.execution.operator

import io.fabric8.kubernetes.api.model.EventBuilder
import io.fabric8.kubernetes.api.model.EventSource
import io.fabric8.kubernetes.api.model.ObjectReference
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import java.time.Instant
import java.util.*

class EventCreator {
    val client: NamespacedKubernetesClient = DefaultKubernetesClient().inNamespace("default")

    fun createEvent(executionName: String, type: String, message: String, reason: String) {
        val uuid = UUID.randomUUID().toString()
        println("uuid is: " + uuid)
        val objectRef = buildObjectReference(executionName)
        val event = EventBuilder()
            .withNewMetadata()
                .withName(uuid)
            .endMetadata()
            .build()

        event.message = message
        event.reason = reason
        event.type = type

        event.firstTimestamp =  Instant.now().toString()

        val source =  EventSource()
        source.component = "theodolite-operator"
        event.source = source

        event.involvedObject = objectRef
        client.v1().events().inNamespace("default").create(event)
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