package theodolite.commons.hazelcastjet;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import org.slf4j.Logger;
import theodolite.commons.hazelcastjet.ConfigurationKeys;

public class BenchmarkConfigBuilder {

  public Config buildFromEnv(final Logger logger, final String bootstrapServerDefault,
      final String hzKubernetesServiceDnsKey) {

    final String bootstrapServer = System.getenv(ConfigurationKeys.BOOTSTRAP_SERVER);
    final String kubernetesDnsName = System.getenv(ConfigurationKeys.KUBERNETES_DNS_NAME);

    ClusterConfig clusterConfig;
    if (bootstrapServer != null) { // NOPMD
      clusterConfig = ClusterConfig.fromBootstrapServer(bootstrapServer);
      logger.info("Use bootstrap server '{}'.", bootstrapServer);
    } else if (kubernetesDnsName != null) { // NOPMD
      clusterConfig = ClusterConfig.fromKubernetesDnsName(kubernetesDnsName);
      logger.info("Use Kubernetes DNS name '{}'.", kubernetesDnsName);
    } else {
      clusterConfig = ClusterConfig.fromBootstrapServer(bootstrapServerDefault);
      logger.info(
          "Neither a bootstrap server nor a Kubernetes DNS name was provided. Use default bootstrap server '{}'.", // NOCS
          bootstrapServerDefault);
    }

    final String port = System.getenv(ConfigurationKeys.PORT);
    if (port != null) {
      clusterConfig.setPort(Integer.parseInt(port));
    }

    final String portAutoIncrement = System.getenv(ConfigurationKeys.PORT_AUTO_INCREMENT);
    if (portAutoIncrement != null) {
      clusterConfig.setPortAutoIncrement(Boolean.parseBoolean(portAutoIncrement));
    }

    final String clusterNamePrefix = System.getenv(ConfigurationKeys.CLUSTER_NAME_PREFIX);
    if (clusterNamePrefix != null) {
      clusterConfig.setClusterNamePrefix(clusterNamePrefix);
    }

    // Set network config for this hazelcast jet instance
    final Config config = new Config()
        .setClusterName(clusterConfig.getClusterNamePrefix());
    final JoinConfig joinConfig = config.getNetworkConfig()
        .setPort(clusterConfig.getPort())
        .setPortAutoIncrement(clusterConfig.isPortAutoIncrement())
        .getJoin();
    joinConfig.getMulticastConfig().setEnabled(false);
    if (clusterConfig.hasBootstrapServer()) {
      joinConfig.getTcpIpConfig().addMember(clusterConfig.getBootstrapServer());
    } else if (clusterConfig.hasKubernetesDnsName()) {
      joinConfig.getKubernetesConfig()
          .setEnabled(true)
          .setProperty(hzKubernetesServiceDnsKey, clusterConfig.getKubernetesDnsName());
    }

    return config;
  }

}
