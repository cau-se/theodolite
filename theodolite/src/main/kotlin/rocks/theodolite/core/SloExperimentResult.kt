package rocks.theodolite.core

/**
 * Represents the outcome of an SLO experiment.
 */
enum class SloExperimentResult {
    /** All SLOs passed for the tested (load, resource) configuration. */
    SUCCESS,
    /** At least one SLO failed for the tested (load, resource) configuration. */
    FAILURE,
    /** The outcome is unknown (e.g., experiment not yet run or an error occurred). */
    UNKNOWN
}
