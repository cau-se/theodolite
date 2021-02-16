package theodolite.benchmark

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.BaseConstructor
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


class BenchmarkYamlParser: Parser {
    override fun <T> parse(path: String, E: Class<T>): T? {
        val input: InputStream = FileInputStream(File(path))
        val parser = Yaml(Constructor(E))
        return parser.loadAs(input, E)
    }
}