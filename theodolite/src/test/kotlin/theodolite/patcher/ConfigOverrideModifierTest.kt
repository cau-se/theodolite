package theodolite.patcher

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.execution.operator.BenchmarkCRDummy
import theodolite.execution.operator.ExecutionCRDummy

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