package rocks.theodolite.benchmarks.commons.hazelcastjet;

import com.hazelcast.config.Config;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import org.slf4j.Logger;

/**
 * Builds JetInstances for Benchmark Implementations in Hazelcast Jet.
 */
public class JetInstanceBuilder {

  private Config config;

  /**
   * Set Hazelcast Config for the JetInstance to be built.
   *
   * @param hazelcastConfig Config for this JetInstance to be built.
   * @return A Uc1JetInstanceBuilder with a set Config.
   */
  public JetInstanceBuilder setCustomConfig(final Config hazelcastConfig) { // NOPMD
    this.config = hazelcastConfig;
    return this;
  }

  /**
   * Sets the ClusterConfig for this builder using the clusterConfigBuilder and environment
   * variables.
   *
   * @param logger A specified logger to log procedures
   * @param bootstrapServerDefault The default bootstrap server used in case no definition by the
   *        environment is provided.
   * @return The Uc1HazelcastJetBuilder factory with a set ClusterConfig.
   */
  public JetInstanceBuilder setConfigFromEnv(final Logger logger, // NOPMD
      final String bootstrapServerDefault, final String hzKubernetesServiceDnsKey) {
    // Use ClusterConfigBuilder to build a cluster config for this microservice
    final BenchmarkConfigBuilder configBuilder = new BenchmarkConfigBuilder();
    this.config =
        configBuilder.buildFromEnv(logger, bootstrapServerDefault, hzKubernetesServiceDnsKey);
    return this;
  }

  /**
   * Builds and returns a {@link JetInstance}. If a config is set, the {@link JetInstance} will
   * contain the set config.
   *
   * @return JetInstance
   */
  public JetInstance build() {
    final JetInstance jet = Jet.newJetInstance();
    final Config localConfig = this.config;
    if (localConfig == null) {
      return jet;
    } else {
      jet.getConfig().setHazelcastConfig(localConfig);
      return jet;
    }

  }

}
