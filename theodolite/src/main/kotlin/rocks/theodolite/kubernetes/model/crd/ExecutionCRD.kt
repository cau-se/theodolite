package rocks.theodolite.kubernetes.model.crd

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.model.annotation.Group
import io.fabric8.kubernetes.model.annotation.Kind
import io.fabric8.kubernetes.model.annotation.Version
import rocks.theodolite.kubernetes.model.BenchmarkExecution

@JsonDeserialize
@Version("v1")
@Group("theodolite.com")
@Kind("execution")
class ExecutionCRD: CustomResource<BenchmarkExecution, ExecutionStatus>(), Namespaced {

    override fun initSpec(): BenchmarkExecution {
        return BenchmarkExecution()
    }

    override fun initStatus(): ExecutionStatus {
         return ExecutionStatus()
    }

}
