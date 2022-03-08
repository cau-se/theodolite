package rocks.theodolite.kubernetes.util.exception

class ActionCommandFailedException(message: String, e: Exception? = null) : DeploymentFailedException(message,e) {
}