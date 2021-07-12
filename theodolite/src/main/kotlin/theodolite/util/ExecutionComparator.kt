package theodolite.util

import theodolite.model.crd.ExecutionCRD
import theodolite.model.crd.States

class ExecutionComparator {

    /**
     * Simple comparator which can be used to order a list of [ExecutionCRD] such that executions with
     * status [States.RESTART] are before all other executions.
     */
    fun compareByState(preferredState: States) = Comparator<ExecutionCRD> { a, b ->
        when {
            (a == null && b == null) -> 0
            (a.status.executionState == preferredState.value) -> -1
            else -> 1
        }
    }
}