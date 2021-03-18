package theodolite.benchmark


import io.fabric8.kubernetes.client.CustomResourceDoneable
import io.fabric8.kubernetes.api.builder.Function

class DonableTestResource(resource: TestResource, function: Function<TestResource,TestResource>) :
    CustomResourceDoneable<TestResource>(resource, function)