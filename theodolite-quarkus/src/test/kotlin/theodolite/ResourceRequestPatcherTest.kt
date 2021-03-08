package theodolite

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.common.constraint.Assert.assertTrue
import org.junit.jupiter.api.Test
import theodolite.k8s.K8sResourceLoader
import theodolite.patcher.PatcherManager
import theodolite.util.PatcherDefinition

/**
 * Resource patcher test
 *
 * This class tested 4 scenarios for the ResourceLimitPatcher and the ResourceRequestPatcher. The different test cases specifies four possible situations:
 * Case 1:  In the given YAML declaration memory and cpu are defined
 * Case 2:  In the given YAML declaration only cpu is defined
 * Case 3:  In the given YAML declaration only memory is defined
 * Case 4:  In the given YAML declaration neither `Resource Request` nor `Request Limit` is defined
 */
@QuarkusTest
class ResourceRequestPatcherTest {
    val testPath = "./src/main/resources/testYaml/"
    val loader = K8sResourceLoader(DefaultKubernetesClient().inNamespace(""))
    val manager = PatcherManager()


    fun applyTest(fileName: String) {
        val CPUValue = "50m"
        val MEMValue = "3Gi"
        val k8sResource = loader.loadK8sResource("Deployment", testPath + fileName) as Deployment

        val defCPU = PatcherDefinition()
        defCPU.variableName = "cpu"
        defCPU.resource = "cpu-memory-deployment.yaml"
        defCPU.container = "uc-application"
        defCPU.type = "ResourceRequestPatcher"

        val defMEM = PatcherDefinition()
        defMEM.variableName = "memory"
        defMEM.resource = "cpu-memory-deployment.yaml"
        defMEM.container = "uc-application"
        defMEM.type = "ResourceRequestPatcher"

        manager.applyPatcher(
            patcherDefinition = listOf(defCPU),
            resources = listOf(Pair("cpu-memory-deployment.yaml", k8sResource)),
            value = CPUValue
        )
        manager.applyPatcher(
            patcherDefinition = listOf(defMEM),
            resources = listOf(Pair("cpu-memory-deployment.yaml", k8sResource)),
            value = MEMValue
        )

        k8sResource.spec.template.spec.containers.filter { it.name == defCPU.container }
            .forEach {
                println(it)
                assertTrue(it.resources.requests["cpu"].toString() == "$CPUValue")
                assertTrue(it.resources.requests["memory"].toString() == "$MEMValue")
            }
    }

    @Test
    fun case1() {
        // Case 1: In the given YAML declaration memory and cpu are defined
        applyTest("cpu-memory-deployment.yaml")
    }
    @Test
    fun case2() {
        // Case 2:  In the given YAML declaration only cpu is defined
        applyTest("cpu-deployment.yaml")
    }
    @Test
    fun case3() {
        //  Case 3:  In the given YAML declaration only memory is defined
        applyTest("memory-deployment.yaml")
    }
    @Test
    fun case4() {
        // Case 4: In the given YAML declaration neither `Resource Request` nor `Request Limit` is defined
        applyTest("no-resources-deployment.yaml")
    }
}