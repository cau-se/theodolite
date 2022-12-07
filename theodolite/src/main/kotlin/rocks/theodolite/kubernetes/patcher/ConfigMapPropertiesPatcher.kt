package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.HasMetadata
import java.io.StringReader
import java.io.StringWriter
import java.util.Properties

/**
 * The ConfigMapYamlPatcher allows to add/modify a key-value pair in a .properties file of a ConfigMap
 *
 * @property fileName of the .properties file in the ConfigMap that should be modified.
 * @property variableName Name of the environment variable to be patched.
 */
class ConfigMapPropertiesPatcher(
    private val fileName: String,
    private val variableName: String,
) : AbstractStringPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        if (resource is ConfigMap) {
            val propertiesFile = resource.data[fileName]

            // Read properties string
            val properties = Properties().also { it.load(StringReader(propertiesFile)) }

            // Change value
            properties.setProperty(this.variableName, value)

            // Convert back to String and set in Kubernetes resource
            val writer = StringWriter()
            properties.store(writer, null)
            resource.data[fileName] = writer.toString()
        }
        return resource
    }
}
