package theodolite.benchmark

interface Parser {
    fun <T> parse(path: String, E:Class<T>): T? //Yaml
}