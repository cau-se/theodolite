package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.api.model.ConfigMap
import io.javaoperatorsdk.operator.api.config.informer.InformerConfiguration
import io.javaoperatorsdk.operator.api.reconciler.Context
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration
import io.javaoperatorsdk.operator.api.reconciler.EventSourceContext
import io.javaoperatorsdk.operator.api.reconciler.EventSourceInitializer
import io.javaoperatorsdk.operator.api.reconciler.Reconciler
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl
import io.javaoperatorsdk.operator.processing.event.ResourceID
import io.javaoperatorsdk.operator.processing.event.source.EventSource
import io.javaoperatorsdk.operator.processing.event.source.PrimaryToSecondaryMapper
import io.javaoperatorsdk.operator.processing.event.source.SecondaryToPrimaryMapper
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource
import mu.KotlinLogging
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.BenchmarkState

private val logger = KotlinLogging.logger {}

/**
 * Passive / observational reconciler for [BenchmarkCRD].
 *
 * **Read-only:** always returns [UpdateControl.noUpdate]; no status patches, no finalizers.
 *
 * Wires a [ConfigMap] [InformerEventSource] so that a change to any ConfigMap referenced
 * by a benchmark's SUT, load-generator, or infrastructure resources triggers a reconcile
 * of that benchmark.  Inside [reconcile] the observed readiness — whether all referenced
 * ConfigMaps are present in the secondary cache — is computed and logged for comparison
 * with the live state managed by [BenchmarkStateChecker].
 *
 * The real [BenchmarkState] written into the CR is still managed exclusively by
 * [BenchmarkStateChecker]; this reconciler observes but does not write.
 */
@ControllerConfiguration
class BenchmarkReconciler : Reconciler<BenchmarkCRD>, EventSourceInitializer<BenchmarkCRD> {

    override fun prepareEventSources(
        context: EventSourceContext<BenchmarkCRD>
    ): Map<String, EventSource> {
        val configMapEventSource = InformerEventSource(
            InformerConfiguration.from(ConfigMap::class.java, context)
                // Benchmark → ConfigMaps: enables getSecondaryResources(ConfigMap) in reconcile.
                .withPrimaryToSecondaryMapper(
                    PrimaryToSecondaryMapper { benchmark: BenchmarkCRD ->
                        configMapNamesOf(benchmark)
                            .map { ResourceID(it, benchmark.metadata.namespace) }
                            .toSet()
                    }
                )
                // ConfigMap → Benchmarks: triggers reconcile when a referenced ConfigMap changes.
                .withSecondaryToPrimaryMapper(
                    SecondaryToPrimaryMapper { configMap: ConfigMap ->
                        context.primaryCache.list().toList()
                            .filter { benchmark -> configMapNamesOf(benchmark).contains(configMap.metadata.name) }
                            .map { ResourceID.fromResource(it) }
                            .toSet()
                    }
                )
                .build(),
            context
        )
        return EventSourceInitializer.nameEventSources(configMapEventSource)
    }

    override fun reconcile(
        resource: BenchmarkCRD,
        context: Context<BenchmarkCRD>
    ): UpdateControl<BenchmarkCRD> {
        val name = resource.metadata.name
        logger.debug { "Reconcile benchmark $name." }

        val referencedConfigMapNames = configMapNamesOf(resource)
        val presentConfigMaps = context.getSecondaryResources(ConfigMap::class.java)
            .map { it.metadata.name }
            .toSet()

        val observedReadiness = if (referencedConfigMapNames.isNotEmpty() &&
            referencedConfigMapNames.all { it in presentConfigMaps }
        ) BenchmarkState.READY else BenchmarkState.PENDING

        val actualState = resource.status.resourceSetsState
        logger.info {
            "Benchmark '$name': observedReadiness=$observedReadiness, actual=$actualState" +
                " (referenced=$referencedConfigMapNames, present=$presentConfigMaps)"
        }

        // Read-only: BenchmarkStateChecker remains the sole status writer.
        return UpdateControl.noUpdate()
    }

    /**
     * Returns the names of all ConfigMaps referenced by the given [benchmark]'s
     * SUT, load-generator, and infrastructure resource sets.
     * Returns an empty set if the spec is not fully initialised (partially constructed
     * objects can appear in tests or during operator startup).
     */
    internal fun configMapNamesOf(benchmark: BenchmarkCRD): Set<String> {
        return try {
            val spec = benchmark.spec
            (spec.sut.resources.mapNotNull { it.configMap?.name } +
                spec.loadGenerator.resources.mapNotNull { it.configMap?.name } +
                spec.infrastructure.resources.mapNotNull { it.configMap?.name })
                .toSet()
        } catch (_: UninitializedPropertyAccessException) {
            emptySet()
        }
    }
}
