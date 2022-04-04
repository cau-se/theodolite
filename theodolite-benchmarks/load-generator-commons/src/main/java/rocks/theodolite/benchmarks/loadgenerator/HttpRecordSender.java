package rocks.theodolite.benchmarks.loadgenerator;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends monitoring records via HTTP.
 *
 * @param <T> {@link SpecificRecord} to send
 */
public class HttpRecordSender<T extends SpecificRecord> implements RecordSender<T> {

  private static final int HTTP_OK = 200;

  private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(1);

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpRecordSender.class);

  private final Gson gson = new Gson();

  private final HttpClient httpClient = HttpClient.newBuilder().build();

  private final URI uri;

  private final boolean async;

  private final Duration connectionTimeout;

  private final List<Integer> validStatusCodes;

  /**
   * Create a new {@link HttpRecordSender}.
   *
   * @param uri the {@link URI} records should be sent to
   */
  public HttpRecordSender(final URI uri) {
    this(uri, false, DEFAULT_CONNECTION_TIMEOUT);
  }

  /**
   * Create a new {@link HttpRecordSender}.
   *
   * @param uri the {@link URI} records should be sent to
   * @param async whether HTTP requests should be sent asynchronous
   * @param connectionTimeout timeout for the HTTP connection
   */
  public HttpRecordSender(final URI uri, final boolean async, final Duration connectionTimeout) {
    this(uri, async, connectionTimeout, List.of(HTTP_OK));
  }

  /**
   * Create a new {@link HttpRecordSender}.
   *
   * @param uri the {@link URI} records should be sent to
   * @param async whether HTTP requests should be sent asynchronous
   * @param connectionTimeout timeout for the HTTP connection
   * @param validStatusCodes a list of HTTP status codes which are considered as successful
   */
  public HttpRecordSender(
      final URI uri,
      final boolean async,
      final Duration connectionTimeout,
      final List<Integer> validStatusCodes) {
    this.uri = uri;
    this.async = async;
    this.connectionTimeout = connectionTimeout;
    this.validStatusCodes = validStatusCodes;
  }

  @Override
  public void send(final T message) {
    final String json = this.gson.toJson(message);
    final HttpRequest request = HttpRequest.newBuilder()
        .uri(this.uri)
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .header("Content-Type", "application/json")
        .timeout(this.connectionTimeout)
        .build();
    final BodyHandler<Void> bodyHandler = BodyHandlers.discarding();
    // final BodyHandler<String> bodyHandler = BodyHandlers.ofString();

    final CompletableFuture<HttpResponse<Void>> result =
        this.httpClient.sendAsync(request, bodyHandler)
            .whenComplete((response, exception) -> {
              if (exception != null) { // NOPMD
                LOGGER.warn("Couldn't send request to {}.", this.uri, exception); // NOPMD false-p.
              } else if (!this.validStatusCodes.contains(response.statusCode())) { // NOPMD
                LOGGER.warn("Received status code {} for request to {}.", response.statusCode(),
                    this.uri);
              } else {
                LOGGER.debug("Sucessfully sent request to {} (status={}).", this.uri,
                    response.statusCode());
              }
            });
    if (this.isSync()) {
      try {
        result.get();
      } catch (final InterruptedException e) {
        LOGGER.error("Couldn't get result for request to {}.", this.uri, e);
      } catch (final ExecutionException e) { // NOPMD
        // Do nothing, Exception is already handled
      }
    }
  }

  private boolean isSync() {
    return !this.async;
  }

}
