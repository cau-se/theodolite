package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Definition of the structure of a patcher.
 */
@RegisterForReflection
class PatcherDefinition {
    /**
     * The type of the patcher
     */
    lateinit var type: String

    /**
     * The resource which the patcher is applied to
     */
    lateinit var resource: String

    /**
     * The container which the patcher is applied to
     */
    lateinit var container: String

    /**
     * The variable name for the patcher
     */
    lateinit var variableName: String
}
