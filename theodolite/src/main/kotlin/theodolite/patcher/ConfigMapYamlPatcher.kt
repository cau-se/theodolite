package theodolite.patcher

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.KubernetesResource
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

/**
 * The ConfigMapYamlPatcher allows to add/modify a key-value pair in a YAML file of a Configmap
 *
 * @property k8sResource ConfigMap kubernetes resource
 * @property fileName of the YAML file in the configmap that should be modified.
 * @property variableName Name of the environment variable to be patched.
 */
class ConfigMapYamlPatcher(
    private val k8sResource: KubernetesResource,
    private val fileName: String,
    private val variableName: String
) : AbstractPatcher(k8sResource) {

    override fun <T> patch(value: T) {
        if (value is String && k8sResource is ConfigMap) {
            val yamlFile = k8sResource.data[fileName]

            //Configure yaml parser
            val dumperOptions = DumperOptions()
            // used to omit curly braces around and new lines for every property
            dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            val parser = Yaml(dumperOptions)

            //Change value
            val yaml = parser.loadAs(yamlFile, LinkedHashMap<String, String>()::class.java)
            yaml[variableName] = value

            //Convert back to String and set in kubernetes resource
            k8sResource.data[fileName] = parser.dump(yaml)
        }
    }
}
