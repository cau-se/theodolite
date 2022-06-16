package rocks.theodolite.kubernetes


open class DeploymentFailedException(message: String, e: Exception? = null) : TheodoliteException(message,e)
