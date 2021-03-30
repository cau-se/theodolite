package theodolite.util

/**
 * Interface for parsing files.
 */
interface Parser {
    fun <T> parse(path: String, E: Class<T>): T?
}
