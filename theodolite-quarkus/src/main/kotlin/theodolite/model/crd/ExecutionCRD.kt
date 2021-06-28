package theodolite.model.crd

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.model.annotation.Group
import io.fabric8.kubernetes.model.annotation.Kind
import io.fabric8.kubernetes.model.annotation.Version
import theodolite.benchmark.BenchmarkExecution

@JsonDeserialize
@Version("v1")
@Group("theodolite.com")
@Kind("execution")
class ExecutionCRD(
    var spec: BenchmarkExecution = BenchmarkExecution(),
    var status: ExecutionStatus = ExecutionStatus()
) : CustomResource<BenchmarkExecution, ExecutionStatus>(), Namespaced
