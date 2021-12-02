package theodolite.commons.workloadgeneration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import org.apache.avro.specific.SpecificRecord;

/**
 * Sends monitoring records via HTTP.
 *
 * @param <T> {@link SpecificRecord} to send
 */
public class HttpRecordSender<T extends SpecificRecord> implements RecordSender<T> {

  // private static final Logger LOGGER = LoggerFactory.getLogger(HttpRecordSender.class);

  private final HttpClient httpClient;

  private final URI uri;

  private final boolean async;

  /**
   * Create a new {@link HttpRecordSender}.
   */
  public HttpRecordSender(final URI uri) {
    this.httpClient = HttpClient.newBuilder().build();
    this.uri = uri;
    this.async = true;
  }

  @Override
  public void send(final T message) {
    final HttpRequest request = HttpRequest.newBuilder()
        .uri(this.uri) // TODO
        .POST(HttpRequest.BodyPublishers.ofString(message.toString())) // TODO to JSON
        .build();
    final BodyHandler<Void> bodyHandler = BodyHandlers.discarding();
    // final BodyHandler<String> bodyHandler = BodyHandlers.ofString();
    if (this.async) {
      this.httpClient.sendAsync(request, bodyHandler);
      // this.httpClient.sendAsync(request, bodyHandler).thenAccept(s -> System.out.println(s));
    } else {
      try {
        this.httpClient.send(request, bodyHandler);
      } catch (IOException | InterruptedException e) {
        throw new IllegalStateException(e); // TODO
      }
    }
  }

}
