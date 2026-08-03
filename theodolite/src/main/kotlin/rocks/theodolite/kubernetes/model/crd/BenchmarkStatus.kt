package rocks.theodolite.kubernetes.model.crd

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Namespaced

@JsonDeserialize
class BenchmarkStatus: KubernetesResource, Namespaced {
    /**
     * The readiness state of the benchmark, or `null` when it has not been computed yet.
     *
     * A freshly applied benchmark has no persisted status, so it deserializes to `null`. This
     * distinguishes "not yet reconciled" from a computed [BenchmarkState.PENDING], allowing the
     * reconciler to persist the initial state even when the first computed state is `PENDING`.
     */
    var resourceSetsState: BenchmarkState? = null

}