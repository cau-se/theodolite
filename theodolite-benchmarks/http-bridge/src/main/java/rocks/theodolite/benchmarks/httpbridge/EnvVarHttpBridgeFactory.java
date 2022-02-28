package rocks.theodolite.benchmarks.httpbridge;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import rocks.theodolite.benchmarks.loadgenerator.ConfigurationKeys;
import rocks.theodolite.benchmarks.loadgenerator.TitanKafkaSenderFactory;
import titan.ccp.model.records.ActivePowerRecord;

class EnvVarHttpBridgeFactory {

  private static final String PORT_KEY = "PORT";
  private static final int PORT_DEFAULT = 8080;

  private static final String HOST_KEY = "HOST";
  private static final String HOST_DEFAULT = "0.0.0.0"; // NOPMD

  private static final String KAFKA_BOOTSTRAP_SERVERS_DEFAULT = "localhost:9092"; // NOPMD
  private static final String KAFKA_TOPIC_DEFAULT = "input";
  private static final String SCHEMA_REGISTRY_URL_DEFAULT = "http://localhost:8081";

  public HttpBridge create() {
    final Endpoint<?> converter = new Endpoint<>(
        "/",
        ActivePowerRecord.class,
        TitanKafkaSenderFactory.forKafkaConfig(
            this.getKafkaBootstrapServer(),
            this.getKafkaTopic(),
            this.getSchemaRegistryUrl()));
    return new HttpBridge(this.getHost(), this.getPort(), List.of(converter));
  }

  private String getHost() {
    return Objects.requireNonNullElse(System.getenv(HOST_KEY), HOST_DEFAULT);
  }

  private int getPort() {
    return Optional.ofNullable(System.getenv(PORT_KEY)).map(Integer::parseInt).orElse(PORT_DEFAULT);
  }

  private String getKafkaBootstrapServer() {
    return Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS),
        KAFKA_BOOTSTRAP_SERVERS_DEFAULT);
  }

  private String getKafkaTopic() {
    return Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_INPUT_TOPIC),
        KAFKA_TOPIC_DEFAULT);
  }

  private String getSchemaRegistryUrl() {
    return Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.SCHEMA_REGISTRY_URL),
        SCHEMA_REGISTRY_URL_DEFAULT);
  }

}
