package theodolite.benchmark

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.BaseConstructor
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


class BenchmarkYamlParser<T>: Parser<T> {
    override fun parse(path: String): KubernetesBenchmark? {
        val input: InputStream = FileInputStream(File(path))
        val parser = Yaml(Constructor(KubernetesBenchmark::class.java))
        return parser.loadAs(input, KubernetesBenchmark::class.java)
    }
}