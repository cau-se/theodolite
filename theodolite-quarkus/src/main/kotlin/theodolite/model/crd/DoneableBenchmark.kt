package theodolite.model.crd

import io.fabric8.kubernetes.api.builder.Function
import io.fabric8.kubernetes.client.CustomResourceDoneable

class DoneableBenchmark(resource: BenchmarkCRD, function: Function<BenchmarkCRD, BenchmarkCRD>) :
    CustomResourceDoneable<BenchmarkCRD>(resource, function)