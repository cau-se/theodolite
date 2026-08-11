package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.api.model.DefaultKubernetesResourceList
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kubernetes.client.KubernetesServer
import io.quarkus.test.kubernetes.client.KubernetesTestServer
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD

/**
 * Uses the CDI-managed [RunnerCoordinator], unlike [RunnerCoordinatorTest] which sets the client
 * directly. Only CDI injects the normal-scoped client proxy that the coordinator has to cope with in
 * production: it implements the bean type [io.fabric8.kubernetes.client.KubernetesClient] but not
 * [io.fabric8.kubernetes.client.NamespacedKubernetesClient].
 */
@WithKubernetesTestServer
@QuarkusTest
internal class RunnerCoordinatorCdiClientTest {

    @KubernetesTestServer
    lateinit var server: KubernetesServer

    @Inject
    lateinit var coordinator: RunnerCoordinator

    @Test
    fun `benchmarks can be listed with the injected client`() {
        this.server.expect()
            .get()
            .withPath("/apis/theodolite.rocks/v1beta2/namespaces/test/benchmarks")
            .andReturn(200, DefaultKubernetesResourceList<BenchmarkCRD>())
            .always()

        assertTrue(this.coordinator.getBenchmarks().isEmpty())
    }
}
