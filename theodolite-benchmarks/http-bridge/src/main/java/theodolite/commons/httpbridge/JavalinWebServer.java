package theodolite.commons.httpbridge;

import io.javalin.Javalin;
import java.util.Collection;

public class JavalinWebServer {

  private static final int HTTP_SUCCESS = 200;

  private final Javalin app = Javalin.create();

  private final String host;
  private final int port;

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
        endpoint.convert(ctx.body());
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
