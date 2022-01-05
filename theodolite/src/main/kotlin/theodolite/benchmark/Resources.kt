package theodolite.benchmark

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.quarkus.runtime.annotations.RegisterForReflection

@JsonDeserialize
@RegisterForReflection
class Resources {

    lateinit var resources: List<ResourceSets>
    lateinit var beforeActions: List<Action>
    lateinit var afterActions: List<Action>

}