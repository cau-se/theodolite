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
import org.junit.Rule;
import org.junit.Test;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;

public class HttpRecordSenderTest {

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

  @Test
  public void testValidUri() {
    this.wireMockRule.stubFor(
        post(urlPathEqualTo("/"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody("received")));

    final HttpRecordSender<ActivePowerRecord> httpRecordSender =
        new HttpRecordSender<>(URI.create("http://localhost:" + this.wireMockRule.port()));
    final ActivePowerRecord record = new ActivePowerRecord("my-id", 12345L, 12.34);
    httpRecordSender.send(record);

    final String expectedJson = "{\"identifier\":\"my-id\",\"timestamp\":12345,\"valueInW\":12.34}";
    verify(exactly(1), postRequestedFor(urlEqualTo("/"))
        .withRequestBody(equalTo(expectedJson))); // toJson
  }

  @Test
  public void testValidUriWithPath() {
    this.wireMockRule.stubFor(
        post(urlPathEqualTo("/post"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody("received")));

    final HttpRecordSender<ActivePowerRecord> httpRecordSender =
        new HttpRecordSender<>(
            URI.create("http://localhost:" + this.wireMockRule.port() + "/post"));
    final ActivePowerRecord record = new ActivePowerRecord("my-id", 12345L, 12.34);
    httpRecordSender.send(record);

    final String expectedJson = "{\"identifier\":\"my-id\",\"timestamp\":12345,\"valueInW\":12.34}";
    verify(exactly(1), postRequestedFor(urlEqualTo("/post"))
        .withRequestBody(equalTo(expectedJson))); // toJson
  }

  @Test
  public void testTimeout() {
    this.wireMockRule.stubFor(
        post(urlPathEqualTo("/"))
            .willReturn(
                aResponse()
                    .withFixedDelay(2_000)
                    .withStatus(200)
                    .withBody("received")));

    final HttpRecordSender<ActivePowerRecord> httpRecordSender =
        new HttpRecordSender<>(URI.create("http://localhost:" + this.wireMockRule.port()));
    final ActivePowerRecord record = new ActivePowerRecord("my-id", 12345L, 12.34);
    httpRecordSender.send(record);

    final String expectedJson = "{\"identifier\":\"my-id\",\"timestamp\":12345,\"valueInW\":12.34}";
    verify(exactly(1), postRequestedFor(urlEqualTo("/"))
        .withRequestBody(equalTo(expectedJson))); // toJson
  }

}
