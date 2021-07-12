package theodolite.execution.operator

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.informers.SharedInformerFactory
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import theodolite.k8s.K8sManager
import theodolite.k8s.K8sResourceLoader
import theodolite.model.crd.States
import java.lang.Thread.sleep


private const val RESYNC_PERIOD = 1000 * 1000.toLong()


@QuarkusTest
class ExecutionEventHandlerTest {
    private final val server = KubernetesServer(false, true)
    private val testResourcePath = "./src/test/resources/k8s-resource-files/"
    private final val executionName = "example-execution"
    lateinit var factory: SharedInformerFactory
    lateinit var executionVersion1: KubernetesResource
    lateinit var executionVersion2: KubernetesResource
    lateinit var stateHandler: ExecutionStateHandler
    lateinit var manager: K8sManager
    lateinit var controller: TheodoliteController

    @BeforeEach
    fun setUp() {
        server.before()
        val operator = TheodoliteOperator()
        this.controller = operator.getController(
            client = server.client,
            executionStateHandler = ExecutionStateHandler(client = server.client)
        )

        this.factory = operator.getExecutionEventHandler(this.controller,server.client)
        this.stateHandler = TheodoliteOperator().getExecutionStateHandler(client = server.client)

        this.executionVersion1 = K8sResourceLoader(server.client)
            .loadK8sResource("Execution", testResourcePath + "test-execution.yaml")

        this.executionVersion2 = K8sResourceLoader(server.client)
            .loadK8sResource("Execution", testResourcePath + "test-execution-update.yaml")

        this.stateHandler = operator.getExecutionStateHandler(server.client)

        this.manager = K8sManager((server.client))
    }

    @AfterEach
    fun tearDown() {
        server.after()
        factory.stopAllRegisteredInformers()
    }

    @Test
    @DisplayName("Check namespaced property of informers")
    fun testNamespaced() {
        manager.deploy(executionVersion1)
        factory.startAllRegisteredInformers()
        server.lastRequest
        // the second request must be namespaced (this is the first `GET` request)
        assert(server
            .lastRequest
            .toString()
            .contains("namespaces")
        )
    }

    @Test
    @DisplayName("Test onAdd method for executions without execution state")
    fun testWithoutState() {
        manager.deploy(executionVersion1)
        factory.startAllRegisteredInformers()
        sleep(500)
        assertEquals(
            States.PENDING,
            stateHandler.getExecutionState(
                resourceName = executionName
            )
        )
    }

    @Test
    @DisplayName("Test onAdd method for executions with execution state `RUNNING`")
    fun testWithStateIsRunning() {
        manager.deploy(executionVersion1)
        stateHandler
            .setExecutionState(
                resourceName = executionName,
                status = States.RUNNING
            )
        factory.startAllRegisteredInformers()
        sleep(500)
        assertEquals(
            States.RESTART,
            stateHandler.getExecutionState(
                resourceName = executionName
            )
        )
    }

    @Test
    @DisplayName("Test onUpdate method for execution with execution state `PENDING`")
    fun testOnUpdatePending() {
        manager.deploy(executionVersion1)

        factory.startAllRegisteredInformers()
        sleep(500)

        assertEquals(
            States.PENDING,
            stateHandler.getExecutionState(
                resourceName = executionName
            )
        )

        manager.deploy(executionVersion2)
        assertEquals(
            States.PENDING,
            stateHandler.getExecutionState(
                resourceName = executionName
            )
        )
    }

    @Test
    @DisplayName("Test onUpdate method for execution with execution state `FINISHED`")
    fun testOnUpdateFinished() {
        manager.deploy(executionVersion1)
        factory.startAllRegisteredInformers()
        sleep(500)

        stateHandler.setExecutionState(
            resourceName = executionName,
            status = States.FINISHED
        )

        manager.deploy(executionVersion2)
        sleep(500)

        assertEquals(
            States.PENDING,
            stateHandler.getExecutionState(
                resourceName = executionName
            )
        )
    }

    @Test
    @DisplayName("Test onUpdate method for execution with execution state `FAILURE`")
    fun testOnUpdateFailure() {
        manager.deploy(executionVersion1)
        factory.startAllRegisteredInformers()
        sleep(500)

        stateHandler.setExecutionState(
            resourceName = executionName,
            status = States.FAILURE
        )

        manager.deploy(executionVersion2)
        sleep(500)

        assertEquals(
            States.PENDING,
            stateHandler.getExecutionState(
                resourceName = executionName
            )
        )
    }


    @Test
    @DisplayName("Test onUpdate method for execution with execution state `RUNNING`")
    fun testOnUpdateRunning() {
        manager.deploy(executionVersion1)
        factory.startAllRegisteredInformers()
        sleep(500)

        stateHandler.setExecutionState(
            resourceName = executionName,
            status = States.RUNNING
        )

        manager.deploy(executionVersion2)
        sleep(500)

        assertEquals(
            States.RESTART,
            stateHandler.getExecutionState(
                resourceName = executionName
            )
        )
    }

    @Test
    @DisplayName("Test onUpdate method for execution with execution state `RESTART`")
    fun testOnUpdateRestart() {
        manager.deploy(executionVersion1)
        factory.startAllRegisteredInformers()
        sleep(500)

        stateHandler.setExecutionState(
            resourceName = executionName,
            status = States.RESTART
        )

        manager.deploy(executionVersion2)
        sleep(500)

        assertEquals(
            States.RESTART,
            stateHandler.getExecutionState(
                resourceName = executionName
            )
        )
    }
}