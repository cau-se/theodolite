package theodolite

import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import mu.KotlinLogging
import java.io.InputStream
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

class DeploymentManager {
    val MEMORYLIMIT = "memory"
    val CPULIMIT = "memory"

    val absolute = Paths.get("").toAbsolutePath().toString()
    val path = "/home/lorenz/git/spesb/theodolite-quarkus/YAML/"
    val theodoliteDeploment = "theodolite.yaml"
    val service = "aggregation-service.yaml"
    val workloadFile = "workloadGenerator.yaml"
    val usecase = "aggregation-deployment.yaml"
    val configMap = "jmx-configmap.yaml"
    val inputStream: InputStream = path.byteInputStream()
    val client = DefaultKubernetesClient().inNamespace("default")


    val dp: Service = client.services().load(path + service).get()

    val workload: Deployment = client.apps().deployments().load(path + workloadFile).get()
    val use: Deployment = client.apps().deployments().load(path + usecase).get()


    val loader = YamlLoader(client)
    val config: ConfigMap? = loader.loadConfigmap(path + configMap)    // TODO ASSEMBLE GOOD SEPERATION

    // TODO REFACTOR EVErythiNG
    fun printFile() {
//        println(workload)
//        changeEnviromentsInstances(workload,"5000")
//        println(workload)

//        logger.debug(use.toString())
//        changeRessourceLimits(use, MEMORYLIMIT, "5Gi")
//        logger.debug(use.toString())

        logger.info(workload.toString())
        val testMap = mapOf<String, String>("NUM_SENSORS" to "5000")
        val vars =
            workload.spec.template.spec.containers.filter { it.name == "workload-generator" }.forEach { it: Container ->
                setContainerEnv(it, testMap)
            }

        logger.info(workload.toString())

        // logger.debug(config.toString())

        //        println(path)
//        val f : File = File(path+theodoliteDeploment);
//        val fileAsString : String = String(f.readBytes())
//        println(fileAsString.replace("theodolite","spesb"))
    }

    /**
     * Sets the ContainerEvironmentVariables, creates new if variable don t exist.
     * @param container - The Container
     * @param map - Map of k=Name,v =Value of EnviromentVariables
     */
    fun setContainerEnv(container: Container, map: Map<String, String>) {
        map.forEach { k, v ->
            // filter for mathing name and set value
            val x = container.env.filter { envvar -> envvar.name == k }

            if (x.isEmpty()) {
                val newVar = EnvVar(k, v, EnvVarSource())
                container.env.add(newVar)
            } else {
                x.forEach {
                    it.value = v
                }
            }
        }
    }

    /**
     * Set the enviroment Variable for a Container
     */
    fun setWorkloadEnv(workloadDeployment: Deployment, containerName: String, map: Map<String, String>) {
        workloadDeployment.spec.template.spec.containers.filter { it.name == containerName }
            .forEach { it: Container ->
                setContainerEnv(it, map)
            }
    }

    /**
     *  Change the RessourceLimit of the SUT
     */
    fun changeRessourceLimits(dep: Deployment, ressource: String, limit: String) {
        val vars = dep.spec.template.spec.containers.filter { it.name == "uc-application" }.forEach {
            it.resources.limits.replace(ressource, Quantity(limit))
        }
    }

    /**
     * Change the image of the SUT and the Worklaodgenerators
     */
    fun changeImage(dep: Deployment, image: String) {

        dep.spec.template.spec.containers.filter { it.name == "uc-application" }.forEach {
            it.image = image
        }
    }
}
