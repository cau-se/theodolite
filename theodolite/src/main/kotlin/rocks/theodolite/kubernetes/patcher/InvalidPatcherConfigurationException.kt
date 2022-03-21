package rocks.theodolite.kubernetes.patcher

import rocks.theodolite.kubernetes.DeploymentFailedException

class InvalidPatcherConfigurationException(message: String, e: Exception? = null) : DeploymentFailedException(message,e)
