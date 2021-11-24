package theodolite.benchmark

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test


@QuarkusTest
class ActionCommandTest {

    @Test
    fun testAction() {
        val action = ActionCommand(DefaultKubernetesClient().inNamespace("default"))
        val result = action.exec(mutableMapOf(
            Pair("app.kubernetes.io/name","grafana")
        ),
            container = "grafana",
            command = "ls /")
        println("out is: " + result.first)
        println("error is: " + result.second)


    }
}