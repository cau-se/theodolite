package rocks.theodolite.kubernetes.operator

import io.javaoperatorsdk.operator.api.reconciler.Context
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration
import io.javaoperatorsdk.operator.api.reconciler.Reconciler
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl
import mu.KotlinLogging
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD

private val logger = KotlinLogging.logger {}

@ControllerConfiguration
class BenchmarkReconciler : Reconciler<BenchmarkCRD> {

    override fun reconcile(resource: BenchmarkCRD, context: Context<BenchmarkCRD>): UpdateControl<BenchmarkCRD> {
        logger.debug { "Reconcile benchmark ${resource.metadata.name}." }

        return UpdateControl.patchStatus(resource)
    }

}