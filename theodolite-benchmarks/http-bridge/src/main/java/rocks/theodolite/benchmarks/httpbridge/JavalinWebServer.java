package rocks.theodolite.benchmarks.httpbridge;

import io.javalin.Javalin;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a webserver based on the Javalin framework.
 */
public class JavalinWebServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(JavalinWebServer.class);

  private static final int HTTP_SUCCESS = 200;

  private final Javalin app = Javalin.create();

  private final String host;
  private final int port;

  /**
   * Create a new instance, running on the specified host and port with the configured endpoints.
   */
  public JavalinWebServer(
      final Collection<Endpoint<?>> converters,
      final String host,
      final int port) {
    this.host = host;
    this.port = port;
    this.configureRoutes(converters);
  }

  private void configureRoutes(final Collection<Endpoint<?>> endpoints) {
    for (final Endpoint<?> endpoint : endpoints) {
      this.app.post(endpoint.getPath(), ctx -> {
        final String record = ctx.body();
        LOGGER.debug("Received record at '{}': {}", ctx.path(), record);
        endpoint.convert(record);
        ctx.status(HTTP_SUCCESS);
      });
    }
  }

  public void start() {
    this.app.start(this.host, this.port);
  }

  public void stop() {
    this.app.close();
  }

}
