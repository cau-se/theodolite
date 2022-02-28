package rocks.theodolite.benchmarks.httpbridge;

import io.javalin.Javalin;
import io.javalin.plugin.metrics.MicrometerPlugin;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a webserver based on the Javalin framework.
 */
public class JavalinWebServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(JavalinWebServer.class);

  private static final int HTTP_SUCCESS = 200;

  private final Javalin app;

  private final PrometheusMeterRegistry registry;

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
    this.registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    this.app = Javalin.create(config -> {
      config.registerPlugin(new MicrometerPlugin(this.registry));
    });
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
    this.app.get("/metrics", ctx -> ctx
        .contentType(TextFormat.CONTENT_TYPE_004)
        .result(this.registry.scrape()));
  }

  public void start() {
    this.app.start(this.host, this.port);
  }

  public void stop() {
    this.app.close();
  }

}
