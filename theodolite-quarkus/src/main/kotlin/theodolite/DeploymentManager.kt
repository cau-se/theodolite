package theodolite

import com.fasterxml.jackson.annotation.JsonProperty
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.dsl.RollableScalableResource
import java.io.File
import java.io.InputStream
import java.nio.file.Paths


class DeploymentManager {

    val absolute = Paths.get("").toAbsolutePath().toString()
    val path = "/home/lorenz/git/spesb/theodolite-quarkus/YAML/"
    val theodoliteDeploment = "theodolite.yaml"
    val service = "aggregation-service.yaml"
    val workloadFile = "workloadGenerator.yaml"
    val inputStream: InputStream = path.byteInputStream()
    val client = DefaultKubernetesClient().inNamespace("default")

    //val deployment = client.apps().deployments().load(absolute + path)

    val dp: Service = client.services().load(path+service).get();

    val workload : Deployment = client.apps().deployments().load(path+workloadFile).get();

    // TODO MAKE YAML LOADING CATCH EXEPTION


    fun printFile(){


        //println(workload)
        changeWorkloadNumInstances(workload,5000)
        //println(workload)


        println(path)
        val f : File = File(path+theodoliteDeploment);
        val fileAsString : String = String(f.readBytes())
        //println(fileAsString.replace("theodolite","spesb"))
    }


    fun changeServiceName (service: Service,newName : String){

        service.metadata.apply {
            name = newName
        }
    }

    fun changeWorkloadNumInstances (dep: Deployment,num: String){

        val vars = dep.spec.template.spec.containers.get(0).env.filter {
            it.name == "NUM_SENSORS"
        }.forEach {
            x ->
                    x.value = num
        }


        println(vars)

    }


}
