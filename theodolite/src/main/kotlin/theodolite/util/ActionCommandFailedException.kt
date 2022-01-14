package theodolite.util

class ActionCommandFailedException(message: String, e: Exception? = null) : DeploymentFailedException(message,e) {
}