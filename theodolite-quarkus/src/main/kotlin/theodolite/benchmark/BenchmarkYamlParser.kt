package theodolite.benchmark

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


class BenchmarkYamlParser<T>: Parser<T> {
    override fun parse(path: String): T {
        val input: InputStream = FileInputStream(File(path))
        val parser = Yaml()
        return parser.load<T>(input)
    }
}