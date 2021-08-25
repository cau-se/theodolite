package theodolite.benchmark

import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

@QuarkusTest
class bugReport {

    private val server = KubernetesServer(false, true)

    @BeforeEach
    fun setUp() {
        server.before()
    }

    @AfterEach
    fun tearDown() {
        server.after()
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun method() {

        val deploymentString = "{\"apiVersion\":\"apps/v1\",\"kind\":\"Deployment\",\"metadata\":{\"finalizers\":[],\"managedFields\":[],\"ownerReferences\":[],\"additionalProperties\":{}},\"spec\":{\"additionalProperties\":{}},\"additionalProperties\":{}}\n"
        val stream =  ByteArrayInputStream(deploymentString.encodeToByteArray())
        val deployment = server.client.apps().deployments().load(stream).get()
        println(deploymentString)
        println(deployment)

    }
}