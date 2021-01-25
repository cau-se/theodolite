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

        deploy(workload)
        logger.info { "Workload deployed" }
        Thread.sleep(Duration(java.time.Duration.ofSeconds(30)).duration.toMillis())
        logger.info { "will delete workload now!" }
        delete(workload)
        logger.info { "workld deletet" }


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
    private fun setContainerEnv(container: Container, map: Map<String, String>) {
        map.forEach { k, v ->
            // filter for mathing name and set value
            val x = container.env.filter { envVar -> envVar.name == k }

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
     * Set the enviroment Variable for a container
     */
    fun setWorkloadEnv(workloadDeployment: Deployment, containerName: String, map: Map<String, String>) {
        workloadDeployment.spec.template.spec.containers.filter { it.name == containerName }
            .forEach { it: Container ->
                setContainerEnv(it, map)
            }
    }

    /**
     *  Change the RessourceLimit of a container (Usally SUT)
     */
    fun changeRessourceLimits(deployment: Deployment, ressource: String, containerName: String, limit: String) {
        val vars = deployment.spec.template.spec.containers.filter { it.name == containerName }.forEach {
            it.resources.limits.replace(ressource, Quantity(limit))
        }
    }

    /**
     * Change the image name of a container (SUT and the Worklaodgenerators)
     */
    fun setImageName(deployment: Deployment, containerName: String, image: String) {
        deployment.spec.template.spec.containers.filter { it.name == containerName }.forEach {
            it.image = image
        }
    }

    //TODO potential add exception handling
    fun deploy(deployment: Deployment) {
        client.apps().deployments().create(deployment)
    }

    //TODO potential add exception handling
    fun delete(deployment: Deployment) {
        client.apps().deployments().delete(deployment)
    }
}
