package rocks.theodolite.kubernetes.util.exception

class EvaluationFailedException(message: String, e: Exception? = null) : ExecutionFailedException(message,e)
