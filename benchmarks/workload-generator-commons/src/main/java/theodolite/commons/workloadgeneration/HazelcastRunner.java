package theodolite.commons.workloadgeneration;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class HazelcastRunner {

  private final HazelcastInstance hzInstance;
  private volatile HazelcastRunnerStateInstance runnerState;
  private final CompletableFuture<Void> stopAction = new CompletableFuture<>();
  private final LoadGeneratorConfig loadConfig;
  private final WorkloadDefinition totalLoadDefinition;

  public HazelcastRunner(
      final ClusterConfig clusterConfig,
      final LoadGeneratorConfig loadConfig,
      final WorkloadDefinition totalLoadDefinition) {
    this.loadConfig = loadConfig;
    this.totalLoadDefinition = totalLoadDefinition;
    this.hzInstance = buildhazelcastInstance(clusterConfig, totalLoadDefinition.toString());
    this.hzInstance.getCluster().addMembershipListener(new RunnerMembershipListener());
  }

  public void runBlocking() {
    while (!this.stopAction.isDone()) {
      synchronized (this) {
        final Set<Member> members = this.hzInstance.getCluster().getMembers();
        this.runnerState = new HazelcastRunnerStateInstance(
            this.loadConfig,
            this.totalLoadDefinition,
            this.hzInstance, members);
      }
      this.runnerState.runBlocking();
    }
  }

  public void restart() {
    this.stopRunnerState();
  }

  public void stop() {
    this.stopAction.complete(null);
    this.stopRunnerState();
  }

  private void stopRunnerState() {
    synchronized (this) {
      if (this.runnerState != null) {
        this.runnerState.stopAsync();
      }
    }
  }

  private class RunnerMembershipListener implements MembershipListener {

    @Override
    public void memberAdded(final MembershipEvent membershipEvent) {
      HazelcastRunner.this.restart();
    }

    @Override
    public void memberRemoved(final MembershipEvent membershipEvent) {
      HazelcastRunner.this.restart();
    }

  }

  private static HazelcastInstance buildhazelcastInstance(
      final ClusterConfig cluster,
      final String clusterName) {
    final Config config = new Config()
        .setClusterName(cluster.getClusterNamePrefix() + '_' + clusterName);

    final JoinConfig joinConfig = config.getNetworkConfig()
        .setPort(cluster.getPort())
        .setPortAutoIncrement(cluster.isPortAutoIncrement())
        .getJoin();
    joinConfig.getMulticastConfig().setEnabled(false);
    // joinConfig.getKubernetesConfig().setEnabled(true);
    joinConfig.getTcpIpConfig().addMember(cluster.getBootstrapServer());

    return Hazelcast.newHazelcastInstance(config);
  }

}
