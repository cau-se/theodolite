package theodolite.util

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * The YamlParser parses a YAML file
 */
class YamlParser : Parser {
    override fun <T> parse(path: String, E: Class<T>): T? {
        val input: InputStream = FileInputStream(File(path))
        val parser = Yaml(Constructor(E))
        return parser.loadAs(input, E)
    }
}
