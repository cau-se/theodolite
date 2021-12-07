package theodolite.commons.hazelcastjet;

import com.hazelcast.config.Config;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import org.slf4j.Logger;
import theodolite.commons.hazelcastjet.BenchmarkConfigBuilder;

public class JetInstanceBuilder {

  private Config config;

  /**
   * Set Hazelcast Config for the JetInstance to be built.
   *
   * @param hazelcastConfig Config for this JetInstance to be built.
   * @return A Uc1JetInstanceBuilder with a set Config.
   */
  public JetInstanceBuilder setCustomConfig(final Config hazelcastConfig) {
    this.config = hazelcastConfig;
    return this;
  }

  /**
   * Sets the ClusterConfig for this builder using the clusterConfigBuilder and environment
   * variables.
   *
   * @param logger A specified logger to log procedures
   * @param BootstrapServerDefault The default bootstrap server used in case no definition by the
   *        environment is provided.
   * @return The Uc1HazelcastJetBuilder factory with a set ClusterConfig.
   */
  public JetInstanceBuilder setConfigFromEnv(final Logger logger,
      final String BootstrapServerDefault, final String hzKubernetesServiceDnsKey) {
    // Use ClusterConfigBuilder to build a cluster config for this microservice
    final BenchmarkConfigBuilder configBuilder = new BenchmarkConfigBuilder();
    final Config config =
        configBuilder.buildFromEnv(logger, BootstrapServerDefault, hzKubernetesServiceDnsKey);
    this.config = config;
    return this;
  }

  /**
   * Builds and returns a JetInstance. If a config is set, the JetInstance will contain the set
   * config.
   *
   * @return
   */
  public JetInstance build() {
    final JetInstance jet = Jet.newJetInstance();
    if (this.config != null) {
      jet.getConfig().setHazelcastConfig(this.config);
      return jet;
    } else {
      return jet;
    }

  }

}
