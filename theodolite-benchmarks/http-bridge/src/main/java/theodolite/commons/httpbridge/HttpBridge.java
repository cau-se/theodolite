package theodolite.commons.httpbridge;

import java.util.List;
import theodolite.commons.workloadgeneration.TitanKafkaSenderFactory;
import titan.ccp.model.records.ActivePowerRecord;

public class HttpBridge {

  private static final int PORT = 8080;
  private static final String HOST = "0.0.0.0"; // NOPMD

  public void run() {
    final Endpoint<?> converter = new Endpoint<>(
        "/",
        ActivePowerRecord.class,
        TitanKafkaSenderFactory.forKafkaConfig(null, null, null));

    final JavalinWebServer webServer = new JavalinWebServer(List.of(converter), HOST, PORT);
    webServer.start();
  }

}
