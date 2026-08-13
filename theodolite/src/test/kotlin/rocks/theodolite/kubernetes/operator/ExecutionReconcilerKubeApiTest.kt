package rocks.theodolite.kubernetes.operator

import com.fasterxml.jackson.databind.ObjectMapper
import io.fabric8.kubeapitest.junit.EnableKubeAPIServer
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.utils.KubernetesSerialization
import io.javaoperatorsdk.operator.junit.LocallyRunOperatorExtension
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.mock
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.BenchmarkState
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionState
import rocks.theodolite.kubernetes.util.KotlinLateinitModule
import java.io.FileInputStream
import java.util.concurrent.TimeUnit
import java.util.logging.ConsoleHandler
import java.util.logging.Logger

/**
 * Runs the [ExecutionReconciler] against a real Kubernetes API server.
 *
 * The test only creates an Execution and lets the Java Operator SDK drive the reconciler. Both
 * patches the framework performs are therefore made exactly as in production: adding the finalizer,
 * which [ExecutionReconciler] requires by implementing `Cleaner`, and patching the status. Both
 * server-side apply the whole custom resource, so both fail if the spec serializes to something the
 * CRD schema does not declare. For the finalizer patch the serialized spec is the one of
 * [ExecutionCRD.initSpec], whose `lateinit` properties are all uninitialized.
 */
@Disabled(
    "Downloads (~167 MB) and runs a real Kubernetes API server under ~/.kubeapitest, which the CI " +
        "pipeline neither caches nor is otherwise set up for. Remove this annotation to run it."
)
@EnableKubeAPIServer
internal class ExecutionReconcilerKubeApiTest {

    companion object {
        /** Injected by the kube-api-test JUnit extension, which requires exactly one such field. */
        @JvmField
        var client: KubernetesClient? = null

        private const val EXECUTION_NAME = "reconciled-execution"

        private val logHandler = ConsoleHandler()

        /**
         * The slf4j binding on the test classpath is slf4j-jboss-logmanager, which discards all
         * output unless a handler is attached. Without this, a failing reconcile is only visible as
         * a timeout below, hiding the API server response that explains it.
         */
        @JvmStatic
        @BeforeAll
        fun attachLogHandler() {
            Logger.getLogger("").addHandler(logHandler)
        }

        @JvmStatic
        @AfterAll
        fun detachLogHandler() {
            Logger.getLogger("").removeHandler(logHandler)
        }
    }

    private val coordinator = mock<RunnerCoordinator>()

    @RegisterExtension
    val operator: LocallyRunOperatorExtension = LocallyRunOperatorExtension.builder()
        .withKubernetesClient(operatorClient())
        .withReconciler(
            ExecutionReconciler().also {
                it.coordinator = this.coordinator
                it.readiness = OperatorReadiness().apply { open() }
            }
        )
        .withAdditionalCRD("crd/crd-execution.yaml", "crd/crd-benchmark.yaml")
        .withConfigurationService { it.withCloseClientOnStop(false) }
        .waitForNamespaceDeletion(false)
        .build()

    @Test
    fun `reconciling a new execution adds a finalizer and does not modify the spec`() {
        this.operator.create(newExecution())

        val reconciled = this.operator.resources(ExecutionCRD::class.java)
            .withName(EXECUTION_NAME)
            .waitUntilCondition(
                { execution ->
                    execution?.status?.executionState == ExecutionState.PENDING &&
                        !execution.metadata.finalizers.isNullOrEmpty()
                },
                60, TimeUnit.SECONDS
            )

        // The finalizer is added by the framework via server-side apply, patching a resource whose
        // spec is the one of ExecutionCRD.initSpec(), i.e. entirely uninitialized.
        assertFalse(reconciled.metadata.finalizers.isEmpty())
        assertEquals(ExecutionState.PENDING, reconciled.status.executionState)
        assertEquals("uc1-kstreams", reconciled.spec.benchmark)
        assertEquals("NumSensors", reconciled.spec.load.loadType)
        assertEquals(listOf(25000, 50000, 75000, 100000, 125000, 150000), reconciled.spec.load.loadValues)
        assertEquals("Instances", reconciled.spec.resources.resourceType)
        assertEquals(listOf(1, 2, 3, 4, 5), reconciled.spec.resources.resourceValues)
        assertEquals("RestrictionSearch", reconciled.spec.execution.strategy.name)
        assertEquals(300L, reconciled.spec.execution.duration)
        assertEquals(1, reconciled.spec.execution.repetitions)
    }

    private fun newExecution(): ExecutionCRD {
        val execution = this.operator.kubernetesClient.resources(ExecutionCRD::class.java)
            .load(FileInputStream("src/test/resources/k8s-resource-files/test-execution.yaml"))
            .item()
        execution.metadata.name = EXECUTION_NAME
        execution.metadata.namespace = this.operator.namespace
        return execution
    }

    /** Client configured like the one of the operator, i.e. with the [KotlinLateinitModule]. */
    private fun operatorClient(): KubernetesClient =
        KubernetesClientBuilder()
            .withConfig(client!!.configuration)
            .withKubernetesSerialization(
                KubernetesSerialization(ObjectMapper().registerModule(KotlinLateinitModule()), true)
            )
            .build()
}

/**
 * Verifies the full [ExecutionState.PENDING] → [ExecutionState.RUNNING] lifecycle against a real
 * Kubernetes API server.
 *
 * The test applies a [BenchmarkCRD] with [BenchmarkState.READY] status and an [ExecutionCRD]
 * that references it, then asserts that the reconciler transitions the execution to RUNNING.
 *
 * A mocked [TheodoliteRunner] is used so that [RunnerCoordinator.triggerSelection] reaches the
 * point where it sets `active` and fires the synthetic reconcile event (which persists RUNNING),
 * without attempting to deploy real Kubernetes resources.
 */
@Disabled(
    "Downloads (~167 MB) and runs a real Kubernetes API server under ~/.kubeapitest, which the CI " +
        "pipeline neither caches nor is otherwise set up for. Remove this annotation to run it."
)
@EnableKubeAPIServer
internal class ExecutionPendingToRunningKubeApiTest {

    companion object {
        @JvmField
        var client: KubernetesClient? = null

        private const val BENCHMARK_NAME = "example-benchmark"
        private const val EXECUTION_NAME = "test-execution"

        private val logHandler = ConsoleHandler()

        @JvmStatic
        @BeforeAll
        fun attachLogHandler() {
            Logger.getLogger("").addHandler(logHandler)
        }

        @JvmStatic
        @AfterAll
        fun detachLogHandler() {
            Logger.getLogger("").removeHandler(logHandler)
        }
    }

    private val runner = mock<TheodoliteRunner>()
    private val coordinator = RunnerCoordinator(runner)

    @RegisterExtension
    val operator: LocallyRunOperatorExtension = LocallyRunOperatorExtension.builder()
        .withKubernetesClient(operatorClient())
        .withReconciler(
            ExecutionReconciler().also {
                it.coordinator = coordinator
                it.readiness = OperatorReadiness().apply { open() }
            }
        )
        .withAdditionalCRD("crd/crd-execution.yaml", "crd/crd-benchmark.yaml")
        .withConfigurationService { it.withCloseClientOnStop(false) }
        .waitForNamespaceDeletion(false)
        .build()

    @BeforeEach
    fun setUpCoordinator() {
        // Set the coordinator's client after the operator extension has resolved the test namespace.
        // adapt() + inNamespace() produces a client whose Config.namespace is the test namespace,
        // so namespacedClient() inside RunnerCoordinator queries and propagates events for the
        // correct namespace.
        coordinator.client = operatorClient()
            .adapt(NamespacedKubernetesClient::class.java)
            .inNamespace(operator.namespace)
    }

    @Test
    fun `execution transitions to running once its benchmark is ready`() {
        val k8sClient = operator.kubernetesClient

        // Apply the benchmark and immediately patch its status to READY so that selectNext()
        // can find an eligible benchmark when the execution is reconciled.
        val benchmark = k8sClient.resources(BenchmarkCRD::class.java)
            .load(FileInputStream("src/test/resources/k8s-resource-files/test-benchmark.yaml"))
            .item()
        benchmark.metadata.namespace = operator.namespace
        k8sClient.resources(BenchmarkCRD::class.java).inNamespace(operator.namespace).resource(benchmark).create()
        benchmark.status.resourceSetsState = BenchmarkState.READY
        k8sClient.resources(BenchmarkCRD::class.java)
            .inNamespace(operator.namespace)
            .resource(benchmark)
            .patchStatus()

        // Apply the execution referencing the benchmark.
        val execution = k8sClient.resources(ExecutionCRD::class.java)
            .load(FileInputStream("src/test/resources/k8s-resource-files/test-execution.yaml"))
            .item()
        execution.metadata.name = EXECUTION_NAME
        execution.metadata.namespace = operator.namespace
        execution.spec.benchmark = BENCHMARK_NAME
        operator.create(execution)

        // The reconciler must drive: NoState → Pending (+ reschedule) → Running.
        operator.resources(ExecutionCRD::class.java)
            .withName(EXECUTION_NAME)
            .waitUntilCondition(
                { exec -> exec?.status?.executionState == ExecutionState.RUNNING },
                30, TimeUnit.SECONDS
            )
    }

    private fun operatorClient(): KubernetesClient =
        KubernetesClientBuilder()
            .withConfig(client!!.configuration)
            .withKubernetesSerialization(
                KubernetesSerialization(ObjectMapper().registerModule(KotlinLateinitModule()), true)
            )
            .build()
}
