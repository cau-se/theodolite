package theodolite.benchmark

import io.fabric8.kubernetes.client.CustomResource

data class TestResource(var spec: TestSpec = TestSpec()): CustomResource() {
}