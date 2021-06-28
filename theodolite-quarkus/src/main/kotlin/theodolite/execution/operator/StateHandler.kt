package theodolite.execution.operator

private const val MAX_TRIES: Int = 5

interface StateHandler<T> {
    fun setState(resourceName: String, f: (T) -> T?)
    fun getState(resourceName: String, f: (T) -> String?): String?
    fun blockUntilStateIsSet(
        resourceName: String,
        desiredStatusString: String,
        f: (T) -> String?,
        maxTries: Int = MAX_TRIES): Boolean

}