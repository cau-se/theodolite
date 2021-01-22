package theodolite
import io.quarkus.runtime.annotations.QuarkusMain
import theodolite.execution.TheodoliteExecutor


@QuarkusMain
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val theodolite = TheodoliteExecutor();
        theodolite.run()
    }
}
