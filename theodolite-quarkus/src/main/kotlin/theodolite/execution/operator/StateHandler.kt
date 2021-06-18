package theodolite.execution.operator

import io.fabric8.kubernetes.client.CustomResource
private const val MAX_TRIES: Int = 5

interface StateHandler {
    fun setState(resourceName: String, f: (CustomResource) -> CustomResource?)
    fun getState(resourceName: String, f: (CustomResource) -> String?): String?
    fun blockUntilStateIsSet(
        resourceName: String,
        desiredStatusString: String,
        f: (CustomResource) -> String?,
        maxTries: Int = MAX_TRIES): Boolean

}