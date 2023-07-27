package rocks.theodolite.kubernetes.util

/**
 * Interface for parsers.
 * A parser allows the reading of files and creates a corresponding object from them.
 */
interface Parser {
    /**
     * Parse a file.
     *
     * @param path The path of the file
     * @param clazz The class of the type to parse
     * @param T The type to parse
     */
    fun <T> parse(path: String, clazz: Class<T>): T?
}
