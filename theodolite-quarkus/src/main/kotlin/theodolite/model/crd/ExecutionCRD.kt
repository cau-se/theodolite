package theodolite.model.crd

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.client.CustomResource
import theodolite.benchmark.BenchmarkExecution

@JsonDeserialize
class ExecutionCRD(
    var spec: BenchmarkExecution = BenchmarkExecution(),
    var status: ExecutionStatus = ExecutionStatus()
    ) : CustomResource(), Namespaced