package rocks.theodolite.kubernetes.util

import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * The YamlParser parses a YAML file
 */
@Deprecated("Use Jackson ObjectMapper instead")
class YamlParserFromFile : Parser {
    override fun <T> parse(path: String, clazz: Class<T>): T? {
        val input: InputStream = FileInputStream(File(path))
        val parser = Yaml(Constructor(clazz, LoaderOptions()))
        return parser.loadAs(input, clazz)
    }
}
