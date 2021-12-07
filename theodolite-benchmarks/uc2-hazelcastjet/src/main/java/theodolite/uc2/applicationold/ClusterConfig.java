package theodolite.uc2.applicationold;

/**
 * Configuration of a load generator cluster.
 */
public final class ClusterConfig {

  private static final int PORT_DEFAULT = 5701;
  private static final String CLUSTER_NAME_PREFIX_DEFAULT = "theodolite-load-generation";

  private final String bootstrapServer;
  private final String kubernetesDnsName;
  private int port = PORT_DEFAULT;
  private boolean portAutoIncrement = true;
  private String clusterNamePrefix = CLUSTER_NAME_PREFIX_DEFAULT;

  /**
   * Create a new {@link ClusterConfig} with the given parameter values.
   */
  private ClusterConfig(final String bootstrapServer, final String kubernetesDnsName) {
    this.bootstrapServer = bootstrapServer;
    this.kubernetesDnsName = kubernetesDnsName;
  }

  public boolean hasBootstrapServer() {
    return this.bootstrapServer != null;
  }

  public String getBootstrapServer() {
    return this.bootstrapServer;
  }

  public boolean hasKubernetesDnsName() {
    return this.kubernetesDnsName != null;
  }

  public String getKubernetesDnsName() {
    return this.kubernetesDnsName;
  }

  public int getPort() {
    return this.port;
  }

  public boolean isPortAutoIncrement() {
    return this.portAutoIncrement;
  }

  public ClusterConfig setPortAutoIncrement(final boolean portAutoIncrement) { // NOPMD
    this.portAutoIncrement = portAutoIncrement;
    return this;
  }

  public ClusterConfig setPort(final int port) { // NOPMD
    this.port = port;
    return this;
  }

  public String getClusterNamePrefix() {
    return this.clusterNamePrefix;
  }

  public ClusterConfig setClusterNamePrefix(final String clusterNamePrefix) { // NOPMD
    this.clusterNamePrefix = clusterNamePrefix;
    return this;
  }

  public static ClusterConfig fromBootstrapServer(final String bootstrapServer) {
    return new ClusterConfig(bootstrapServer, null);
  }

  public static ClusterConfig fromKubernetesDnsName(final String kubernetesDnsName) {
    return new ClusterConfig(null, kubernetesDnsName);
  }

}
