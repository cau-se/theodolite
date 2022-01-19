package theodolite.commons.workloadgeneration;

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
import com.google.gson.Gson;
import java.net.URI;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import titan.ccp.model.records.ActivePowerRecord;

public class HttpRecordSenderTest {

  private HttpRecordSender<ActivePowerRecord> httpRecordSender;

  private Gson gson;

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

  @Before
  public void setup() {
    this.httpRecordSender =
        new HttpRecordSender<>(URI.create("http://localhost:" + this.wireMockRule.port()));
    this.gson = new Gson();
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

    verify(exactly(1), postRequestedFor(urlEqualTo("/"))
        .withRequestBody(equalTo(this.gson.toJson(record)))); // toJson
  }

}
