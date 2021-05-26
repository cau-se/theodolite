package theodolite.model.crd

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.client.CustomResource
import theodolite.benchmark.KubernetesBenchmark

@JsonDeserialize
class BenchmarkCRD(
    var spec: KubernetesBenchmark = KubernetesBenchmark()
) : CustomResource(), Namespaced