package rocks.theodolite.kubernetes.slo

import com.fasterxml.jackson.databind.node.BooleanNode
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rocks.theodolite.core.SloExperimentResult

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
            return emptyMap()
        }

        override fun stop() {
            wireMockServer.stop()
        }
    }

    @Test
    fun testExternalTrueResult() {
        WireMockTestResource.wireMockServer.stubFor(
            post(urlEqualTo("/"))
                .willReturn(aResponse().withJsonBody(BooleanNode.getTrue()))
        )

        val sloChecker = ExternalSloChecker(
            externalSlopeURL = "${WireMockTestResource.wireMockServer.baseUrl()}/",
            metadata = emptyMap()
        )
        val result = sloChecker.evaluate(emptyList())
        assertEquals(SloExperimentResult.SUCCESS, result)
    }

    @Test
    fun testExternalFalseResult() {
        WireMockTestResource.wireMockServer.stubFor(
            post(urlEqualTo("/"))
                .willReturn(aResponse().withJsonBody(BooleanNode.getFalse()))
        )

        val sloChecker = ExternalSloChecker(
            externalSlopeURL = "${WireMockTestResource.wireMockServer.baseUrl()}/",
            metadata = emptyMap()
        )
        val result = sloChecker.evaluate(emptyList())
        assertEquals(SloExperimentResult.FAILURE, result)
    }
}
