package rocks.theodolite.kubernetes.operator

private const val MAX_RETRIES: Int = 5

@Deprecated("should not be needed")
interface StateHandler<T> {

    fun setState(resourceName: String, f: (T) -> T?)

    fun getState(resourceName: String, f: (T) -> String?): String?

    fun blockUntilStateIsSet(
        resourceName: String,
        desiredStatusString: String,
        f: (T) -> String?,
        maxRetries: Int = MAX_RETRIES
    ): Boolean

}