package rocks.theodolite.kubernetes

class ActionCommandFailedException(message: String, e: Exception? = null) : DeploymentFailedException(message,e) {
}