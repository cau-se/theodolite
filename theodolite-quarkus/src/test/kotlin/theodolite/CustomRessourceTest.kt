package theodolite

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.common.constraint.Assert.assertTrue
import org.junit.jupiter.api.Test
import theodolite.k8s.K8sResourceLoader
import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition
import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.api.model.HasMetadata

import io.fabric8.kubernetes.internal.KubernetesDeserializer
import java.io.File
import javax.json.Json


@QuarkusTest
class CustomRessourceTest {

    val client = DefaultKubernetesClient()
    val loader = K8sResourceLoader(client)
    val path = "./src/main/resources/yaml/"

    @Test
    fun aids(){
        val crd = loader.loadK8sResource("ServiceMonitor", path + "uc1-service-monitor.yaml")


        val crds = client.apiextensions().v1beta1().customResourceDefinitions().list()

        println(crd.toString())
//        val context = CustomResourceDefinitionContext.Builder().build()
            //.withGroup()
            //.withScope()
            //.withPlural()
            //.withKind("ServiceMonitor").build()
            //.withName("kafka")
//
//        println(client.customResource(context).list("default"))
//
//        client.customResource(context).create(crd.toString())

        val crdsItems = crds.getItems()
        println("Found ${crdsItems.size} CRD(s)")
        var dummy: CustomResourceDefinition? = null
        val dummyCRDName: String = "servicemonitors.monitoring.coreos.com"
        for (crd in crdsItems) {
            val metadata: ObjectMeta = crd.getMetadata()
            if (metadata != null) {
                val name = metadata.name
                //println("    " + name + " => " + metadata.selfLink)
                if (dummyCRDName.equals(name)) {
                    dummy = crd;
                    println("dummy found")
                }
            }

        }
        val context  = CustomResourceDefinitionContext.fromCrd(dummy)

        val customclient = client.customResource(context)

        println(customclient.list())
        val f = File(path + "uc1-service-monitor.yaml")


        val customr = customclient.load(f.inputStream())
        println("Customressource: $customr")

        println("Dummy: $dummy")

        customclient.create(customr)

        }

    //@Test
    fun loadTest(){
        val crd = loader.loadK8sResource("ServiceMonitor", path + "uc1-service-monitor.yaml")

        println(crd)
    }
}