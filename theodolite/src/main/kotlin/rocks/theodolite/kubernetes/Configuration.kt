package rocks.theodolite.kubernetes

// Defaults
private const val DEFAULT_NAMESPACE = "default"
private const val DEFAULT_COMPONENT_NAME = "theodolite-operator"
private const val DEFAULT_PROMETHEUS_URL = "http://prometheus-operated:9090"
private const val DEFAULT_PROMETHEUS_OFFSET_HOURS = "0"


class Configuration {
    companion object {
        val NAMESPACE = System.getenv("NAMESPACE") ?: DEFAULT_NAMESPACE
        val COMPONENT_NAME = System.getenv("COMPONENT_NAME") ?: DEFAULT_COMPONENT_NAME
        val EXECUTION_MODE = System.getenv("MODE") ?: ExecutionModes.OPERATOR.value

        /** Default Prometheus URL used when an SLI does not specify one in its providerConfig. */
        val PROMETHEUS_URL = System.getenv("THEODOLITE_PROMETHEUS_URL") ?: DEFAULT_PROMETHEUS_URL

        /** Default Prometheus offset in hours used when an SLI does not specify one in its providerConfig. */
        val PROMETHEUS_OFFSET_HOURS = (System.getenv("THEODOLITE_PROMETHEUS_OFFSET_HOURS") ?: DEFAULT_PROMETHEUS_OFFSET_HOURS).toLong()

        /**
         * Specifies how long Theodolite should wait (in sec) before aborting the execution of an action command.
         */
        const val TIMEOUT_SECONDS: Long = 30L
    }

}
