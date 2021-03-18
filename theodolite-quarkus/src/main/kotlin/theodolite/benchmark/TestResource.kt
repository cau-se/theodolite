package theodolite.benchmark

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.client.CustomResource

@JsonDeserialize
data class TestResource(var name:String): CustomResource() {

}