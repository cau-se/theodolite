package theodolite.util

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

/**
 * The YamlParser parses a YAML string
 */
class YamlParserFromString : Parser {
    override fun <T> parse(fileString: String, E: Class<T>): T? {
        val parser = Yaml(Constructor(E))
        return parser.loadAs(fileString, E)
    }
}
