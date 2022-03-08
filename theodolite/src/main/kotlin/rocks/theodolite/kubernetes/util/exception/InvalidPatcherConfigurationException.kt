package rocks.theodolite.kubernetes.util.exception

class InvalidPatcherConfigurationException(message: String, e: Exception? = null) : DeploymentFailedException(message,e)
