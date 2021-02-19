package theodolite.util

interface Parser {
    fun <T> parse(path: String, E:Class<T>): T?
}