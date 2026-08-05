package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.javaoperatorsdk.operator.api.config.informer.Informer
import io.javaoperatorsdk.operator.api.config.informer.InformerEventSourceConfiguration
import io.javaoperatorsdk.operator.api.reconciler.Constants
import io.javaoperatorsdk.operator.api.reconciler.Context
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration
import io.javaoperatorsdk.operator.api.reconciler.EventSourceContext
import io.javaoperatorsdk.operator.api.reconciler.Reconciler
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl
import io.javaoperatorsdk.operator.processing.event.ResourceID
import io.javaoperatorsdk.operator.processing.event.source.EventSource
import io.javaoperatorsdk.operator.processing.event.source.PrimaryToSecondaryMapper
import io.javaoperatorsdk.operator.processing.event.source.SecondaryToPrimaryMapper
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource
import jakarta.inject.Inject
import mu.KotlinLogging
import rocks.theodolite.kubernetes.Action
import rocks.theodolite.kubernetes.ExecActionSelector
import rocks.theodolite.kubernetes.ResourceSets
import rocks.theodolite.kubernetes.loadKubernetesResources
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.BenchmarkState

private val logger = KotlinLogging.logger {}

/**
 * Reconciler for [BenchmarkCRD].
 *
 * Computes and writes [BenchmarkState] as the **sole** status writer for benchmarks.
 * A benchmark is [BenchmarkState.READY] when:
 * 1. All ConfigMaps referenced by its SUT and load-generator resource sets are present
 *    (detected via the [ConfigMap] [InformerEventSource] secondary cache), and
 * 2. Every exec-command action can be satisfied — i.e. the target pod is either already
 *    running in the cluster or declared as infrastructure in the benchmark spec.
 *
 * Reconcile returns [UpdateControl.patchStatus] when the computed state differs from the
 * current CR status, and [UpdateControl.noUpdate] otherwise (idempotent).
 */
@ControllerConfiguration(informer = Informer(namespaces = [Constants.WATCH_CURRENT_NAMESPACE]))
class BenchmarkReconciler : Reconciler<BenchmarkCRD> {

    /**
     * Kubernetes client used for live pod/infrastructure checks inside [checkActionCommands].
     * Set automatically by CDI in production; set directly in tests that exercise the
     * action-command logic.  Left unset in tests that only verify ConfigMap-based readiness
     * (those benchmarks carry no exec actions so the field is never accessed).
     */
    @Inject
    lateinit var client: KubernetesClient

    @Inject
    lateinit var readiness: OperatorReadiness

    override fun prepareEventSources(
        context: EventSourceContext<BenchmarkCRD>
    ): List<EventSource<*, BenchmarkCRD>> {
        val configMapEventSource = InformerEventSource(
            InformerEventSourceConfiguration.from(ConfigMap::class.java, BenchmarkCRD::class.java)
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
        return listOf(configMapEventSource)
    }

    override fun reconcile(
        resource: BenchmarkCRD,
        context: Context<BenchmarkCRD>
    ): UpdateControl<BenchmarkCRD> {
        val name = resource.metadata.name
        logger.debug { "Reconcile benchmark '$name'." }

        if (!readiness.isReady()) {
            return UpdateControl.noUpdate<BenchmarkCRD>().rescheduleAfter(OperatorReadiness.RETRY_INTERVAL)
        }

        val desiredState = computeReadiness(resource, context)
        val currentState = resource.status.resourceSetsState

        return if (desiredState != currentState) {
            logger.info { "Benchmark '$name': state $currentState → $desiredState." }
            patchState(resource, desiredState)
            UpdateControl.noUpdate()
            // TODO: once the operator-sdk is upgraded to a version based on JOSDK >= 5.1.4
            //  (operator-framework/java-operator-sdk#2943, which makes the status patch diff only
            //  the status), replace the patchState(...) + noUpdate() workaround above with the
            //  idiomatic single-writer form:
            //      resource.status.resourceSetsState = desiredState
            //      UpdateControl.patchStatus(resource)
        } else {
            logger.debug { "Benchmark '$name': state already $currentState, no update." }
            UpdateControl.noUpdate()
        }
    }

    /**
     * Writes [desiredState] into the benchmark's status via the fabric8 client rather than through
     * [UpdateControl.patchStatus]. The bundled operator-sdk builds the status patch as a diff over
     * the whole resource (metadata + spec + status), which the API server rejects with 422 on the
     * /status subresource. Fetching the current resource and patching only its status sends a
     * status-only request.
     *
     * TODO: remove this workaround and go back to [UpdateControl.patchStatus] once the operator-sdk
     *  is upgraded to a version based on JOSDK >= 5.1.4 (see
     *  operator-framework/java-operator-sdk#2943). Note: `quarkus.operator-sdk.enable-ssa=true` does
     *  not help on the current version, as it only affects primary-resource patches, not the status.
     */
    private fun patchState(resource: BenchmarkCRD, desiredState: BenchmarkState) {
        val crdClient = client.resources(BenchmarkCRD::class.java).inNamespace(resource.metadata.namespace)
        val current = crdClient.withName(resource.metadata.name).get() ?: return
        current.status.resourceSetsState = desiredState
        crdClient.withName(resource.metadata.name).patchStatus(current)
    }

    internal fun computeReadiness(resource: BenchmarkCRD, context: Context<BenchmarkCRD>): BenchmarkState {
        return try {
            if (checkResources(resource, context) && checkActionCommands(resource)) {
                BenchmarkState.READY
            } else {
                BenchmarkState.PENDING
            }
        } catch (_: UninitializedPropertyAccessException) {
            BenchmarkState.PENDING
        }
    }

    /**
     * Returns `true` when both the SUT and the load-generator sections each contain at least
     * one resource set **and** every ConfigMap-backed resource set in those sections is present
     * in the secondary cache (i.e. exists in the cluster and has been observed by the
     * [InformerEventSource]).
     *
     * `fileSystem`-backed resource sets are treated as always available: they are local files
     * mounted into the operator pod and do not require a cluster-side watch. A section that
     * consists entirely of filesystem resource sets is therefore considered ready as long as it
     * is non-empty.
     */
    private fun checkResources(resource: BenchmarkCRD, context: Context<BenchmarkCRD>): Boolean {
        val spec = resource.spec
        val presentConfigMaps = context.getSecondaryResources(ConfigMap::class.java)
            .map { it.metadata.name }.toSet()
        return checkSection(spec.sut.resources, presentConfigMaps) &&
            checkSection(spec.loadGenerator.resources, presentConfigMaps)
    }

    /**
     * Returns `true` when [resources] is non-empty and every ConfigMap-backed entry in
     * [resources] is present in [presentConfigMapNames].  FileSystem entries count towards
     * "non-empty" but do not require a cache entry.
     */
    private fun checkSection(resources: List<ResourceSets>, presentConfigMapNames: Set<String>): Boolean {
        if (resources.isEmpty()) return false
        val requiredConfigMaps = resources.mapNotNull { it.configMap?.name }.toSet()
        return requiredConfigMaps.all { it in presentConfigMapNames }
    }

    /**
     * Returns `true` when every exec-command action defined on the benchmark's SUT and
     * load-generator can be satisfied.  An action is satisfiable if:
     * - it is a delete command (no pod required), or
     * - the target pod is currently running in the cluster, or
     * - the target pod is declared as a Deployment or StatefulSet in the benchmark's infrastructure.
     *
     * Benchmarks without exec actions return `true` immediately (no live API call is made).
     */
    private fun checkActionCommands(resource: BenchmarkCRD): Boolean {
        val spec = resource.spec
        val infraResources = spec.infrastructure.resources
        val actionGroups = listOf(
            spec.sut.beforeActions,
            spec.sut.afterActions,
            spec.loadGenerator.beforeActions,
            spec.loadGenerator.afterActions
        )

        val hasExecActions = actionGroups.any { actions -> actions.any { it.execCommand != null } }
        if (!hasExecActions) return true

        // DefaultKubernetesClient (what Quarkus CDI produces) implements NamespacedKubernetesClient,
        // so this cast is safe at runtime.
        @Suppress("UNCHECKED_CAST")
        val namespacedClient = (client as NamespacedKubernetesClient)
            .inNamespace(resource.metadata.namespace)
        return actionGroups.all { actions -> checkIfActionPossible(infraResources, actions, namespacedClient) }
    }

    private fun checkIfActionPossible(
        resourceSets: List<ResourceSets>,
        actions: List<Action>,
        client: NamespacedKubernetesClient
    ): Boolean {
        return actions.all { action ->
            action.deleteCommand != null ||
                checkIfResourceIsDeployed(action.execCommand!!.selector, client) ||
                checkIfResourceIsInfrastructure(resourceSets, action.execCommand!!.selector, client)
        }
    }

    /**
     * Returns `true` when at least one running pod matches the label selector (and
     * optional container name) of [selector].
     */
    internal fun checkIfResourceIsDeployed(
        selector: ExecActionSelector,
        client: NamespacedKubernetesClient
    ): Boolean {
        val pods = client.pods().withLabels(selector.pod.matchLabels).list().items
        return if (pods.isNotEmpty() && selector.container.isNotEmpty()) {
            pods.any { pod -> pod.spec.containers.any { c -> c.name == selector.container } }
        } else {
            pods.isNotEmpty()
        }
    }

    /**
     * Returns `true` when the benchmark's infrastructure contains a Deployment or
     * StatefulSet whose labels match [selector] (and optionally whose containers include
     * [ExecActionSelector.container]).
     */
    internal fun checkIfResourceIsInfrastructure(
        resourceSets: List<ResourceSets>,
        selector: ExecActionSelector,
        client: NamespacedKubernetesClient
    ): Boolean {
        val resources = loadKubernetesResources(resourceSets, client)
        if (resources.isEmpty()) return false
        val resourceList = resources.map { it.second }

        val inDeployment = resourceList.filterIsInstance<Deployment>()
            .filter { it.metadata.labels.containsMatchLabels(selector.pod.matchLabels) }
            .any { d ->
                selector.container.isEmpty() ||
                    d.spec.template.spec.containers.any { it.name == selector.container }
            }
        if (inDeployment) return true

        return resourceList.filterIsInstance<StatefulSet>()
            .filter { it.metadata.labels.containsMatchLabels(selector.pod.matchLabels) }
            .any { ss ->
                selector.container.isEmpty() ||
                    ss.spec.template.spec.containers.any { it.name == selector.container }
            }
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

private fun <K, V> Map<K, V>.containsMatchLabels(matchLabels: Map<K, V>): Boolean {
    for (kv in matchLabels) {
        if (kv.value != this[kv.key]) {
            return false
        }
    }
    return true
}
