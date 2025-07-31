package rocks.theodolite.kubernetes.slo

import com.fasterxml.jackson.databind.node.BooleanNode
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
@QuarkusTestResource(ExternalSloCheckerTest.WireMockTestResource::class)
internal class ExternalSloCheckerTest {
    internal class WireMockTestResource : QuarkusTestResourceLifecycleManager {

        companion object {
            lateinit var wireMockServer: WireMockServer private set
        }

        override fun start(): Map<String, String> {
            wireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
            wireMockServer.start()
            return mapOf("slo.checker.url" to wireMockServer.baseUrl())
        }

        override fun stop() {
            wireMockServer.stop()
        }
    }



    @Test
    fun testExternalTrueResult() {
        WireMockTestResource.wireMockServer.stubFor(
            post(urlEqualTo("/"))
                .willReturn(
                    aResponse().withJsonBody(BooleanNode.getTrue())
                )
        )

        val sloChecker = ExternalSloChecker(
            mapOf()
        )
        val result = sloChecker.evaluate(listOf())
        assertTrue(result)
    }

    @Test
    fun testExternalFalseResult() {
        WireMockTestResource.wireMockServer.stubFor(
            post(urlEqualTo("/"))
                .willReturn(
                    aResponse().withJsonBody(BooleanNode.getFalse())
                )
        )

        val sloChecker = ExternalSloChecker(
            mapOf()
        )
        val result = sloChecker.evaluate(listOf())
        assertFalse(result)
    }

}
