package rocks.theodolite.kubernetes.slo

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager

/**
 * Shared WireMock server used by the metric fetcher tests. It also provides the DQL OAuth
 * configuration (normally read from env vars / application.properties) and points the auth
 * endpoint at the WireMock server.
 */
internal class WireMockTestResource : QuarkusTestResourceLifecycleManager {
    companion object {
        lateinit var wireMockServer: WireMockServer private set
    }

    override fun start(): Map<String, String> {
        wireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
        wireMockServer.start()
        return mapOf(
            "dql.clientid" to "test-client-id",
            "dql.clientsecret" to "test-client-secret",
            "dql.scope" to "storage:logs:read",
            "dql.resource" to "urn:dtaccount:test",
            "dql.authurl" to "${wireMockServer.baseUrl()}/sso/oauth2/token"
        )
    }

    override fun stop() {
        wireMockServer.stop()
    }
}
