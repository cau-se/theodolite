package theodolite.commons.workloadgeneration;

public class ClusterConfig {
  public static final String BOOTSTRAP_SERVER_DEFAULT = "localhost:5701";
  public static final int PORT_DEFAULT = 5701;
  public static final boolean PORT_AUTO_INCREMENT_DEFAULT = true;
  public static final String CLUSTER_NAME_PREFIX_DEFAULT = "theodolite-load-generation";

  private final String bootstrapServer;
  private final int port;
  private final boolean portAutoIncrement;
  private final String clusterNamePrefix;

  public ClusterConfig() {
    this(
        BOOTSTRAP_SERVER_DEFAULT,
        PORT_DEFAULT,
        PORT_AUTO_INCREMENT_DEFAULT,
        CLUSTER_NAME_PREFIX_DEFAULT);
  }

  public ClusterConfig(final String bootstrapServer, final int port,
      final boolean portAutoIncrement, final String clusterNamePrefix) {
    this.bootstrapServer = bootstrapServer;
    this.port = port;
    this.portAutoIncrement = portAutoIncrement;
    this.clusterNamePrefix = clusterNamePrefix;
  }

  public String getBootstrapServer() {
    return this.bootstrapServer;
  }

  public int getPort() {
    return this.port;
  }

  public boolean isPortAutoIncrement() {
    return this.portAutoIncrement;
  }

  public String getClusterNamePrefix() {
    return this.clusterNamePrefix;
  }


}
