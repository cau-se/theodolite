package theodolite.benchmark

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.quarkus.runtime.annotations.RegisterForReflection

@JsonDeserialize
@RegisterForReflection
class Resources {

    lateinit var resources: List<ResourceSets>

}