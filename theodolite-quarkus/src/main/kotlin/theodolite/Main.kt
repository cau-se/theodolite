package theodolite
import io.quarkus.runtime.annotations.QuarkusMain


@QuarkusMain
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        println("Running main method")

        val run = RunUc()

        val testtopic = mapOf<String,Int>("test" to 1)

        run.createTopics(testtopic, 1.toShort())
        run.deleteTopics(listOf("test"))
        //Quarkus.run()
    }
}
