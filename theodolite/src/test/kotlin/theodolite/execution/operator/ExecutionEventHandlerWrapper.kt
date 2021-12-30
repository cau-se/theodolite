package theodolite.execution.operator

import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import theodolite.model.crd.ExecutionCRD

class ExecutionEventHandlerWrapper(
    private val executionEventHandler: ExecutionEventHandler,
    private val afterOnAddCallback: () -> Unit,
    private val afterOnUpdateCallback: () -> Unit,
    private val afterOnDeleteCallback: () -> Unit
) : ResourceEventHandler<ExecutionCRD> {

    override fun onAdd(execution: ExecutionCRD) {
        this.executionEventHandler.onAdd(execution)
        this.afterOnAddCallback()
    }

    override fun onUpdate(oldExecution: ExecutionCRD, newExecution: ExecutionCRD) {
        this.executionEventHandler.onUpdate(oldExecution, newExecution)
        this.afterOnUpdateCallback()
    }

    override fun onDelete(execution: ExecutionCRD, deletedFinalStateUnknown: Boolean) {
        this.executionEventHandler.onDelete(execution, deletedFinalStateUnknown)
        this.afterOnDeleteCallback()
    }
}