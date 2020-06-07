package theodolite.commons.workloadgeneration.misc;

/*
 * Wrapper for connection information for ZooKeeper.
 */
public class ZooKeeper {

  private final String host;
  private final int port;

  /**
   * Create a new representation of an ZooKeeper instance.
   *
   * @param host of zookeeper.
   * @param port of zookeeper.
   */
  public ZooKeeper(final String host, final int port) {
    super();
    this.host = host;
    this.port = port;
  }

  public String getHost() {
    return this.host;
  }

  public int getPort() {
    return this.port;
  }
}
