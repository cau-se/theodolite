package rocks.theodolite.benchmarks.httpbridge;

import java.util.List;
import theodolite.commons.workloadgeneration.RecordSender;

/**
 * Class that creates a webserver with potentially multiple {@link Endpoint}s, which receives JSON
 * objects at these endpoints, converts them to Java objects and send them using
 * {@link RecordSender}s.
 */
public class HttpBridge {

  private final JavalinWebServer webServer;

  public HttpBridge(final String host, final int port, final List<Endpoint<?>> converters) {
    this.webServer = new JavalinWebServer(converters, host, port);
  }

  public void start() {
    this.webServer.start();
  }

  public void stop() {
    this.webServer.stop();
  }

  public void runAsStandalone() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> this.stop()));
    this.start();
  }

  public static HttpBridge fromEnvironment() {
    return new EnvVarHttpBridgeFactory().create();
  }

  public static void main(final String[] args) {
    HttpBridge.fromEnvironment().runAsStandalone();
  }

}
