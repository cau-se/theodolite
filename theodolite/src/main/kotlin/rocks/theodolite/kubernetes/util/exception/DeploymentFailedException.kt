package rocks.theodolite.kubernetes.util.exception


open class DeploymentFailedException(message: String, e: Exception? = null) : TheodoliteException(message,e)
