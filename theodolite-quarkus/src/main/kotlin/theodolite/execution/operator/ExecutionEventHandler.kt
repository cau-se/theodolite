package theodolite.execution.operator

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.model.crd.*

private val logger = KotlinLogging.logger {}

/**
 * Handles adding, updating and deleting BenchmarkExecutions.
 *
 * @param controller The TheodoliteController that handles the application state
 *
 * @see TheodoliteController
 * @see BenchmarkExecution
 */
class ExecutionHandler(
    private val controller: TheodoliteController,
    private val stateHandler: ExecutionStateHandler
) : ResourceEventHandler<ExecutionCRD> {
    private val gson: Gson = GsonBuilder().enableComplexMapKeySerialization().create()

    /**
     * Add an execution to the end of the queue of the TheodoliteController.
     *
     * @param ExecutionCRD the execution to add
     */
    @Synchronized
    override fun onAdd(execution: ExecutionCRD) {
        logger.info { "Add execution ${execution.metadata.name}" }
        execution.spec.name = execution.metadata.name
        when (this.stateHandler.getExecutionState(execution.metadata.name)) {
            null -> this.stateHandler.setExecutionState(execution.spec.name, STATES.Pending)
            STATES.Running -> {
                this.stateHandler.setExecutionState(execution.spec.name, STATES.Restart)
                if(this.controller.isExecutionRunning(execution.spec.name)){
                    this.controller.stop(restart=true)
                    }
                }
        }
    }

    /**
     * Updates an execution. If this execution is running at the time this function is called, it is stopped and
     * added to the beginning of the queue of the TheodoliteController.
     * Otherwise, it is just added to the beginning of the queue.
     *
     * @param oldExecutionCRD the old execution
     * @param newExecutionCRD the new execution
     */
    @Synchronized
    override fun onUpdate(oldExecution: ExecutionCRD, newExecution: ExecutionCRD) {
        logger.info { "Receive update event for execution ${oldExecution.metadata.name}" }
        newExecution.spec.name = newExecution.metadata.name
        oldExecution.spec.name = oldExecution.metadata.name
        if(gson.toJson(oldExecution.spec) != gson.toJson(newExecution.spec)) {
            when(this.stateHandler.getExecutionState(newExecution.metadata.name)) {
                STATES.Running -> {
                        this.stateHandler.setExecutionState(newExecution.spec.name, STATES.Restart)
                         if (this.controller.isExecutionRunning(newExecution.spec.name)){
                            this.controller.stop(restart=true)
                            }
                        }
                STATES.Restart -> {} // should this set to pending?
                else -> this.stateHandler.setExecutionState(newExecution.spec.name, STATES.Pending)
                }
            }
        }

    /**
     * Delete an execution from the queue of the TheodoliteController.
     *
     * @param ExecutionCRD the execution to delete
     */
    @Synchronized
    override fun onDelete(execution: ExecutionCRD, b: Boolean) {
        logger.info { "Delete execution ${execution.metadata.name}" }
         if(execution.status.executionState == STATES.Running.value
             && this.controller.isExecutionRunning(execution.spec.name)) {
            this.controller.stop()
        }
    }
}
