package rocks.theodolite.kubernetes.slo

import com.fasterxml.jackson.databind.node.BooleanNode
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
internal class ExternalSloCheckerTest {

    private var wireMockServer: WireMockServer? = null

    @BeforeEach
    fun start() {
        wireMockServer = WireMockServer().also {
            it.start()
        }
    }

    @AfterEach
    fun stop() {
        wireMockServer?.stop()
    }

    @Test
    fun testExternalTrueResult() {
        this.wireMockServer!!.stubFor(
            post(urlEqualTo("/"))
                .willReturn(
                    aResponse().withJsonBody(BooleanNode.getTrue())
                )
        )

        val sloChecker = ExternalSloChecker(
            wireMockServer!!.baseUrl(),
            mapOf()
        )
        val result = sloChecker.evaluate(listOf())
        assertTrue(result)
    }

    @Test
    fun testExternalFalseResult() {
        this.wireMockServer!!.stubFor(
            post(urlEqualTo("/"))
                .willReturn(
                    aResponse().withJsonBody(BooleanNode.getFalse())
                )
        )

        val sloChecker = ExternalSloChecker(
            wireMockServer!!.baseUrl(),
            mapOf()
        )
        val result = sloChecker.evaluate(listOf())
        assertFalse(result)
    }

}