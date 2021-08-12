package theodolite.execution.operator

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.quarkus.test.junit.QuarkusTest
import mu.KotlinLogging
import org.junit.jupiter.api.Test

private val logger = KotlinLogging.logger {}


@QuarkusTest
class testTest {


    @Test
    fun test(){
        val operator = TheodoliteOperator()
        val client = DefaultKubernetesClient().inNamespace("default")
        val benchmarkClient = operator.getBenchmarkClient(client = client)
        val benchmarks = benchmarkClient
            .list()
            .items

        val r = benchmarks.map{
            it.spec.loadKubernetesResources(it.spec.loadGenResourceSets)
        }



        r.forEach {
            it?.forEach{
                logger.info { it }
            }
        }

    }
}