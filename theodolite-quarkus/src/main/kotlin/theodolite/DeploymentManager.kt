package theodolite

import com.fasterxml.jackson.annotation.JsonProperty
import io.fabric8.kubernetes.api.model.Quantity
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.dsl.RollableScalableResource
import java.io.File
import java.io.InputStream
import java.nio.file.Paths


class DeploymentManager {
    val MEMORYLIMIT = "memory"
    val CPULIMIT = "memory"

    val absolute = Paths.get("").toAbsolutePath().toString()
    val path = "/home/lorenz/git/spesb/theodolite-quarkus/YAML/"
    val theodoliteDeploment = "theodolite.yaml"
    val service = "aggregation-service.yaml"
    val workloadFile = "workloadGenerator.yaml"
    val usecase = "aggregation-deployment.yaml"
    val inputStream: InputStream = path.byteInputStream()
    val client = DefaultKubernetesClient().inNamespace("default")

    //val deployment = client.apps().deployments().load(absolute + path)

    val dp: Service = client.services().load(path+service).get();

    val workload : Deployment = client.apps().deployments().load(path+workloadFile).get();
    val use : Deployment = client.apps().deployments().load(path+usecase).get();


    // TODO MAKE YAML LOADING CATCH EXEPTION


    fun printFile(){

//
//        println(workload)
//        changeWorkloadNumInstances(workload,"5000")
//        println(workload)

        println(use)
        changeRessourceLimits(use, MEMORYLIMIT,"5Gi")
        println(use)


//        println(path)
//        val f : File = File(path+theodoliteDeploment);
//        val fileAsString : String = String(f.readBytes())
//        println(fileAsString.replace("theodolite","spesb"))
    }


    // SERVICE
    fun changeServiceName (service: Service,newName : String){

        service.metadata.apply {
            name = newName
        }
    }

    // WORKLOAD GEN
    fun changeWorkloadNumInstances (dep: Deployment,num: String) {

        dep.spec.template.spec.containers.get(0).env.filter {
            it.name == "NUM_SENSORS"
        }.forEach { x ->
            x.value = num
        }
    }

    // APPLICATION
    fun changeRessourceLimits(dep: Deployment, ressource: String, limit: String) {

        val vars = dep.spec.template.spec.containers.filter { it.name == "uc-application" }.forEach {
            it.resources.limits.replace(ressource, Quantity(limit))
        }
    }

    fun changeImage(dep: Deployment, image: String) {

        dep.spec.template.spec.containers.filter { it.name == "uc-application" }.forEach {
            it.image = image }
    }

}
