package rocks.theodolite.kubernetes.operator

import io.javaoperatorsdk.operator.api.reconciler.Context
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration
import io.javaoperatorsdk.operator.api.reconciler.Reconciler
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl
import mu.KotlinLogging
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD

private val logger = KotlinLogging.logger {}

@ControllerConfiguration
class ExecutionReconciler : Reconciler<ExecutionCRD> {

    override fun reconcile(resource: ExecutionCRD, context: Context<ExecutionCRD>): UpdateControl<ExecutionCRD> {
        logger.debug { "Reconcile resource ${resource.metadata.name}." }
        // Is benchmark available/ready?
        // -> set status isBenchmarkAvailable = true/false
        //

        // Is execution executed at the moment?
        // ->
        return UpdateControl.patchStatus(resource)
    }

}