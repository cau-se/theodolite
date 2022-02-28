package rocks.theodolite.benchmarks.loadgenerator;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.net.URI;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import titan.ccp.model.records.ActivePowerRecord;

public class HttpRecordSenderTest {

  private HttpRecordSender<ActivePowerRecord> httpRecordSender;

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

  @Before
  public void setup() {
    this.httpRecordSender =
        new HttpRecordSender<>(URI.create("http://localhost:" + this.wireMockRule.port()));
  }

  @Test
  public void testValidUri() {
    this.wireMockRule.stubFor(
        post(urlPathEqualTo("/"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody("received")));

    final ActivePowerRecord record = new ActivePowerRecord("my-id", 12345L, 12.34);
    this.httpRecordSender.send(record);

    final String expectedJson = "{\"identifier\":\"my-id\",\"timestamp\":12345,\"valueInW\":12.34}";
    verify(exactly(1), postRequestedFor(urlEqualTo("/"))
        .withRequestBody(equalTo(expectedJson))); // toJson
  }

}
