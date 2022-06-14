package rocks.theodolite.kubernetes

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@JsonDeserialize
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
class DeleteCommand {

    lateinit var selector: DeleteActionSelector

    fun exec(client: NamespacedKubernetesClient) {
        logger.info { "Deleting all resources with apiVersion ${selector.apiVersion} and Kind ${selector.kind} matching regular expression ${selector.nameRegex}" }
        val regExp = selector.nameRegex.toRegex()
        val k8sManager = K8sManager(client)
        client
            .genericKubernetesResources(selector.apiVersion, selector.kind)
            .inNamespace(client.namespace)
            .list()
            .items
            .filter { regExp.matches(it.metadata.name) }
            .forEach{
                logger.info { "Deleting resource ${it.metadata.name} of Kind ${it.kind} and api/version ${it.apiVersion}." }
                try {
                    k8sManager.remove(it)
                } catch (e: KubernetesClientException) {
                    logger.error { "An error occured when deleting resource ${it.metadata.name} of Kind ${it.kind} and api/version ${it.apiVersion}. Error: ${e.message}"}
                }
            }
    }
}

@JsonDeserialize
@RegisterForReflection
class DeleteActionSelector {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    lateinit var apiVersion: String
    @JsonInclude(JsonInclude.Include.NON_NULL)
    lateinit var kind: String
    @JsonInclude(JsonInclude.Include.NON_NULL)
    lateinit var nameRegex: String
}