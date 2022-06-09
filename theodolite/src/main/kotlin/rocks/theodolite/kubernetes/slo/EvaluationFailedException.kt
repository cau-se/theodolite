package rocks.theodolite.kubernetes.slo

import rocks.theodolite.kubernetes.ExecutionFailedException

class EvaluationFailedException(message: String, e: Exception? = null) : ExecutionFailedException(message,e)
