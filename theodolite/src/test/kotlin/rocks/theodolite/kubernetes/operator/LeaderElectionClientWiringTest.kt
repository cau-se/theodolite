package rocks.theodolite.kubernetes.operator

import com.fasterxml.jackson.databind.ObjectMapper
import io.javaoperatorsdk.operator.api.config.ConfigurationService
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD

/**
 * Guards the serialization wiring of the Kubernetes client that the Java Operator SDK actually uses.
 *
 * JOSDK serializes the whole custom resource for its server-side apply patches (e.g. adding the
 * finalizer that [ExecutionReconciler] requires via `Cleaner`). A freshly created Execution has an
 * uninitialized `lateinit` spec, so this only works if JOSDK's client carries the
 * [rocks.theodolite.kubernetes.util.KotlinLateinitModule].
 *
 * Providing leader election through a `ConfigurationServiceCustomizer` used to break this: it makes
 * quarkus-operator-sdk wrap the configuration in an `OverriddenConfigurationService` that does not
 * delegate `getKubernetesClient()`, so JOSDK fell back to a default client without the module and
 * every finalizer patch failed in production. This test exercises exactly that client, so it fails
 * if leader election (or anything else) is ever wired through a customizer again. See
 * [LeaderElectionProducer].
 */
@WithKubernetesTestServer
@QuarkusTest
internal class LeaderElectionClientWiringTest {

    @Inject
    lateinit var configurationService: ConfigurationService

    @Test
    fun `the client used by the operator serializes an uninitialized Execution spec`() {
        val crd = ExecutionCRD().apply { metadata.name = "test-execution" }

        val serialization = this.configurationService.kubernetesClient.kubernetesSerialization
        val json = ObjectMapper().readTree(serialization.asJson(crd))

        assertTrue(json.at("/spec").isObject)
        assertFalse(json.at("/spec").fieldNames().hasNext())
    }
}
