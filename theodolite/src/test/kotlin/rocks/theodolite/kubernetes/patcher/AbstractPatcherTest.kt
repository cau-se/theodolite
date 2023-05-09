package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder
import io.quarkus.test.junit.QuarkusTest

@QuarkusTest
abstract class AbstractPatcherTest {

    lateinit var resource: List<HasMetadata>
    lateinit var patcher: Patcher
    lateinit var value: String

    fun createDeployment(): Deployment {
        return DeploymentBuilder()
            .withNewMetadata()
                .withName("dummy")
            .endMetadata()
            .withNewSpec()
                .withNewSelector()
                    .withMatchLabels<String, String>(mapOf("labelName" to "labelValue"))
                .endSelector()
                    .withNewTemplate()
                        .withNewMetadata()
                            .withLabels<String, String>(mapOf("labelName" to "labelValue"))
                        .endMetadata()
                        .withNewSpec()
                        .withContainers(
                                ContainerBuilder()
                                    .withName("container")
                                    .withImage("test-image")
                                    .build())
                            .addNewVolume()
                                .withName("test-volume")
                                .withNewConfigMap()
                                    .withName("test-configmap")
                                .endConfigMap()
                            .endVolume()
                        .endSpec()
                .endTemplate()
            .endSpec()
            .build()
    }

    fun createStatefulSet(): StatefulSet {
        return StatefulSetBuilder()
            .withNewMetadata()
                .withName("dummy")
            .endMetadata()
            .withNewSpec()
                .withNewSelector()
                    .withMatchLabels<String, String>(mapOf("labelName" to "labelValue"))
                .endSelector()
                .withNewTemplate()
                    .withNewMetadata()
                        .withLabels<String, String>(mapOf("labelName" to "labelValue"))
                    .endMetadata()
                    .withNewSpec()
                    .addNewVolume()
                        .withName("test-volume")
                            .withNewConfigMap()
                                .withName("test-configmap")
                            .endConfigMap()
                        .endVolume()
                    .endSpec()
                .endTemplate()
            .endSpec()
            .build()
    }

    fun createService(): Service {
        return ServiceBuilder()
            .withNewMetadata()
            .withName("dummy")
            .endMetadata()
            .build()
    }

    fun createConfigMap(): ConfigMap {
        return ConfigMapBuilder()
            .withNewMetadata()
                .withName("dummy")
            .endMetadata()
            .withData<String, String>(mapOf("application.properties" to "propA = valueA"))
            .build()
    }

    fun patch() {
        resource = patcher.patch(resource, value)
    }

}