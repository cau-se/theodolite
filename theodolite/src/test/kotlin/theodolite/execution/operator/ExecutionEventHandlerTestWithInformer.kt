package theodolite.execution.operator

import io.fabric8.kubernetes.client.dsl.Resource
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kubernetes.client.KubernetesTestServer
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import theodolite.model.crd.ExecutionCRD
import theodolite.model.crd.ExecutionStates
import java.io.FileInputStream
import java.util.concurrent.CountDownLatch
import java.util.stream.Stream

@WithKubernetesTestServer
@QuarkusTest
class ExecutionEventHandlerTestWithInformer {

    @KubernetesTestServer
    private lateinit var server: KubernetesServer

    lateinit var executionClient: ExecutionClient

    lateinit var controller: TheodoliteController

    lateinit var stateHandler: ExecutionStateHandler

    lateinit var addCountDownLatch: CountDownLatch
    lateinit var updateCountDownLatch: CountDownLatch
    lateinit var deleteCountDownLatch: CountDownLatch

    lateinit var eventHandler: ExecutionEventHandlerWrapper

    @BeforeEach
    fun setUp() {
        server.before()

        this.server.client
            .apiextensions().v1()
            .customResourceDefinitions()
            .load(FileInputStream("crd/crd-execution.yaml"))
            .create()

        this.executionClient = this.server.client.resources(ExecutionCRD::class.java)

        this.controller = mock()
        this.stateHandler = ExecutionStateHandler(server.client)
        this.addCountDownLatch = CountDownLatch(1)
        this.updateCountDownLatch = CountDownLatch(2)
        this.deleteCountDownLatch = CountDownLatch(1)
        this.eventHandler = ExecutionEventHandlerWrapper(
            ExecutionEventHandler(this.controller, this.stateHandler),
            { addCountDownLatch.countDown() },
            { updateCountDownLatch.countDown() },
            { deleteCountDownLatch.countDown() }
        )
    }

    @AfterEach
    fun tearDown() {
        server.after()
        this.server.client.informers().stopAllRegisteredInformers()
    }

    @Test
    fun testCrdRegistered() {
        val crds = this.server.client.apiextensions().v1().customResourceDefinitions().list();
        assertEquals(1, crds.items.size)
        assertEquals("execution", crds.items[0].spec.names.kind)
    }

    @Test
    fun testExecutionDeploy() {
        getExecutionFromSystemResource("k8s-resource-files/test-execution.yaml").create()

        val executions = executionClient.list().items
        assertEquals(1, executions.size)
    }

    @Test
    fun testStatusSet() {
        val execCreated = getExecutionFromSystemResource("k8s-resource-files/test-execution.yaml").create()
        assertNotNull(execCreated.status)
        val execResponse = this.executionClient.withName(execCreated.metadata.name)
        val execResponseItem = execResponse.get()
        assertNotNull(execResponseItem.status)
    }

    @Test
    @DisplayName("Test onAdd method for executions without execution state")
    fun testOnAddWithoutStatus() {
        // Create first version of execution resource
        val executionResource = getExecutionFromSystemResource("k8s-resource-files/test-execution.yaml")
        val execution = executionResource.create()
        val executionName = execution.metadata.name

        // Start informer
        this.executionClient.inform(eventHandler)

        // Await informer called
        this.addCountDownLatch.await()
        assertEquals(ExecutionStates.PENDING.value, this.executionClient.withName(executionName).get().status.executionState)
    }

    @Test
    @DisplayName("Test onAdd method for executions with execution state `RUNNING`")
    fun testOnAddWithStatusRunning() {
        // Create first version of execution resource
        val executionResource = getExecutionFromSystemResource("k8s-resource-files/test-execution.yaml")
        val executionName = executionResource.get().metadata.name

        whenever(this.controller.isExecutionRunning(executionName)).thenReturn(true)

        // Start informer
        this.executionClient.inform(eventHandler)

        val execution = executionResource.create()

        // Update status of execution
        execution.status.executionState = ExecutionStates.RUNNING.value
        executionResource.patchStatus(execution)

        // Assert that status at server matches set status
        // assertEquals(ExecutionStates.RUNNING.value, this.executionClient.withName(executionName).get().status.executionState)

        // Await informer called
        this.addCountDownLatch.await()
        verify(this.controller).stop(true)
        assertEquals(ExecutionStates.RESTART.value, this.executionClient.withName(executionName).get().status.executionState)
    }

    @Test
    @DisplayName("Test onUpdate method for execution with no status")
    fun testOnUpdateWithoutStatus() {
        // Create first version of execution resource
        val firstExecutionResource = getExecutionFromSystemResource("k8s-resource-files/test-execution.yaml")
        val firstExecution = firstExecutionResource.create()
        val executionName = firstExecution.metadata.name

        // Start informer
        this.executionClient.inform(eventHandler)

        // Get execution from server
        val firstExecutionResponse = this.executionClient.withName(executionName).get()
        // Assert that execution at server has pending status
        assertEquals(ExecutionStates.PENDING.value, firstExecutionResponse.status.executionState)

        // Create new version of execution and update at server
        getExecutionFromSystemResource("k8s-resource-files/test-execution-update.yaml").createOrReplace()

        // Await informer called
        this.updateCountDownLatch.await()
        // Get execution from server and assert that new status matches expected one
        assertEquals(ExecutionStates.PENDING.value, this.executionClient.withName(executionName).get().status.executionState)
    }

    @ParameterizedTest
    @MethodSource("provideOnUpdateTestArguments")
    @DisplayName("Test onUpdate method for execution with different status")
    fun testOnUpdateWithStatus(beforeState: ExecutionStates, expectedState: ExecutionStates) {
        // Create first version of execution resource
        val firstExecutionResource = getExecutionFromSystemResource("k8s-resource-files/test-execution.yaml")
        val firstExecution = firstExecutionResource.create()
        val executionName = firstExecution.metadata.name

        // Update status of execution
        firstExecution.status.executionState = beforeState.value
        firstExecutionResource.patchStatus(firstExecution)

        // Start informer
        this.executionClient.inform(eventHandler)

        // Get execution from server
        val firstExecutionResponse = this.executionClient.withName(executionName).get()
        // Assert that status at server matches set status
        assertEquals(beforeState.value, firstExecutionResponse.status.executionState)

        // Create new version of execution and update at server
        getExecutionFromSystemResource("k8s-resource-files/test-execution-update.yaml").createOrReplace()

        // Await informer called
        this.updateCountDownLatch.await()
        // Get execution from server and assert that new status matches expected one
        assertEquals(expectedState.value, this.executionClient.withName(executionName).get().status.executionState)
    }

    @Test
    @Disabled("Informer also called onAdd and changes status")
    fun testOnDeleteWithExecutionRunning() {
        // Create first version of execution resource
        val firstExecutionResource = getExecutionFromSystemResource("k8s-resource-files/test-execution.yaml")
        val firstExecution = firstExecutionResource.create()
        val executionName = firstExecution.metadata.name

        // Update status of execution to be running
        firstExecution.status.executionState = ExecutionStates.RUNNING.value
        firstExecutionResource.patchStatus(firstExecution)

        // Get execution from server
        val firstExecutionResponse = this.executionClient.withName(executionName).get()
        // Assert that execution created at server
        assertNotNull(firstExecutionResponse)

        // Start informer
        this.executionClient.inform(eventHandler)

        // We consider execution to be running
        whenever(this.controller.isExecutionRunning(executionName)).thenReturn(true)

        // Delete execution
        this.executionClient.delete(firstExecutionResponse)

        // Get execution from server
        val secondExecutionResponse = this.executionClient.withName(executionName).get()
        // Assert that execution deleted at server
        assertNull(secondExecutionResponse)

        // Await informer called
        this.deleteCountDownLatch.await()

        verify(this.controller).stop(false)
    }

    @Test
    fun testOnDeleteWithExecutionNotRunning() {
        // Create first version of execution resource
        val firstExecutionResource = getExecutionFromSystemResource("k8s-resource-files/test-execution.yaml")
        val firstExecution = firstExecutionResource.create()
        val executionName = firstExecution.metadata.name

        // Update status of execution to be running
        firstExecution.status.executionState = ExecutionStates.RUNNING.value
        firstExecutionResource.patchStatus(firstExecution)

        // Get execution from server
        val firstExecutionResponse = this.executionClient.withName(executionName).get()
        // Assert that execution created at server
        assertNotNull(firstExecutionResponse)

        // Start informer
        this.executionClient.inform(eventHandler)

        // We consider execution to be running
        whenever(this.controller.isExecutionRunning(executionName)).thenReturn(false)

        // Delete execution
        this.executionClient.delete(firstExecutionResponse)

        // Get execution from server
        val secondExecutionResponse = this.executionClient.withName(executionName).get()
        // Assert that execution created at server
        assertNull(secondExecutionResponse)

        // Await informer called
        this.deleteCountDownLatch.await()

        verify(this.controller, never()).stop(false)
    }

    private fun getExecutionFromSystemResource(resourceName: String): Resource<ExecutionCRD> {
        return executionClient.load(ClassLoader.getSystemResourceAsStream(resourceName))
    }

    companion object {
        @JvmStatic
        fun provideOnUpdateTestArguments(): Stream<Arguments> =
            Stream.of(
                // before state -> expected state
                Arguments.of(ExecutionStates.PENDING, ExecutionStates.PENDING),
                Arguments.of(ExecutionStates.FINISHED, ExecutionStates.PENDING),
                Arguments.of(ExecutionStates.FAILURE, ExecutionStates.PENDING),
                // Arguments.of(ExecutionStates.RUNNING, ExecutionStates.RESTART), // see testOnDeleteWithExecutionRunning
                Arguments.of(ExecutionStates.RESTART, ExecutionStates.RESTART)
            )
    }

}