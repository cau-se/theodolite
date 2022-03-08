package rocks.theodolite.kubernetes.util.exception

open class ExecutionFailedException(message: String, e: Exception? = null) : TheodoliteException(message,e)
