package theodolite.benchmark

interface Parser<T> {
    fun parse(path: String): T //Yaml
}