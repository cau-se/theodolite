package theodolite.execution.operator

import io.fabric8.kubernetes.client.CustomResourceDoneable
import io.fabric8.kubernetes.api.builder.Function
import theodolite.model.crd.ExecutionCRD

class DoneableExecution(resource: ExecutionCRD, function: Function<ExecutionCRD, ExecutionCRD>) :
    CustomResourceDoneable<ExecutionCRD>(resource, function)