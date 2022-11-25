package rocks.theodolite.kubernetes.patcher

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRDummy
import rocks.theodolite.kubernetes.model.crd.ExecutionCRDummy

@QuarkusTest
class ConfigOverrideModifierTest {
    private var execution = BenchmarkExecution()
    private var benchmark = KubernetesBenchmark()


    @BeforeEach
    fun setup() {
        val execution1 = ExecutionCRDummy(name = "matching-execution", benchmark = "Test-Benchmark")
        val benchmark1 = BenchmarkCRDummy(name = "Test-Benchmark")

        this.execution = execution1.getCR().spec
        this.benchmark = benchmark1.getCR().spec
    }


    @Test
    fun setAdditionalLabelsTest() {

        val modifier = ConfigOverrideModifier(
            execution = this.execution,
            resources = listOf("test-resource.yaml")
        )

        modifier.setAdditionalLabels(
            labelName = "test-name",
            labelValue = "test-value"
        )

        Assertions.assertEquals(
            "test-name",
            this.execution
                .configOverrides.firstOrNull()
                ?.patcher
                ?.properties
                ?.get("variableName")
        )
        Assertions.assertEquals(
            "test-value",
            this.execution
                .configOverrides.firstOrNull()
                ?.value
        )
    }
}