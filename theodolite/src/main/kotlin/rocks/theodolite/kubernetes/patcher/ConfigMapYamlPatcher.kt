package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.HasMetadata
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

/**
 * The ConfigMapYamlPatcher allows to add/modify a key-value pair in a YAML file of a ConfigMap
 *
 * @property fileName of the YAML file in the configmap that should be modified.
 * @property variableName Name of the environment variable to be patched.
 */
class ConfigMapYamlPatcher(
    private val fileName: String,
    private val variableName: String,
) : AbstractStringPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        if (resource is ConfigMap) {
            val yamlFile = resource.data[fileName]

            // Configure YAML parser
            val dumperOptions = DumperOptions()
            // used to omit curly braces around and new lines for every property
            dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            val parser = Yaml(dumperOptions)

            // Change value
            val yaml = parser.loadAs(yamlFile, LinkedHashMap<String, String>()::class.java)
            yaml[variableName] = value

            // Convert back to String and set in Kubernetes resource
            resource.data[fileName] = parser.dump(yaml)
        }
        return resource
    }
}
