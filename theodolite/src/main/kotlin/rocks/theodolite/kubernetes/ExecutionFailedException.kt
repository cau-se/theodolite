package rocks.theodolite.kubernetes

open class ExecutionFailedException(message: String, e: Exception? = null) : TheodoliteException(message,e)
