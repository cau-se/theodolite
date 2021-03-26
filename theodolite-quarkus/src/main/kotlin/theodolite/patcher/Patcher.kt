package theodolite.patcher

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
interface Patcher {
    fun <T> patch(value: T)
}
