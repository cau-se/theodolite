package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Definition of the structure of a patcher.
 */
@RegisterForReflection
class PatcherDefinition {
    lateinit var type: String
    lateinit var resource: String
    lateinit var container: String
    lateinit var variableName: String
}
