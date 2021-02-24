package theodolite.commons.workloadgeneration;

import com.google.common.collect.Streams;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicReference;
import com.hazelcast.cp.lock.FencedLock;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instance of a Hazelcast runner state, that is a load generator cluster with a given set of
 * members.
 */
public class HazelcastRunnerStateInstance {

  private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastRunnerStateInstance.class);

  private static final Duration BEFORE_ACTION_WAIT_DURATION = Duration.ofMillis(500);
  private static final Duration TASK_ASSIGNMENT_WAIT_DURATION = Duration.ofMillis(500);

  private final CompletableFuture<Void> stopAction = new CompletableFuture<>();
  private LoadGeneratorExecution loadGeneratorExecution;

  private final LoadGeneratorConfig loadGeneratorConfig;
  private final WorkloadDefinition totalLoadDefinition;
  private final HazelcastInstance hzInstance;
  private final Set<Member> members;

  /**
   * Create a new {@link HazelcastRunnerStateInstance}.
   */
  public HazelcastRunnerStateInstance(
      final LoadGeneratorConfig loadGeneratorConfig,
      final WorkloadDefinition totalLoadDefinition,
      final HazelcastInstance hzInstance,
      final Set<Member> members) {
    this.hzInstance = hzInstance;
    this.members = members;
    this.loadGeneratorConfig = loadGeneratorConfig;
    this.totalLoadDefinition = totalLoadDefinition;

    LOGGER.info("Created new Hazelcast runner instance for member set '{}'", this.members);
  }

  /**
   * Start and block load generation for the configured member set.
   */
  public void runBlocking() {
    if (!this.stopAction.isDone()) {
      this.tryPerformBeforeAction();
      this.tryCreateTaskAssignment();
      this.startLoadGeneration();
    }
    this.stopAction.join();
    this.stopLoadGeneration();
  }

  public void stopAsync() {
    this.stopAction.complete(null);
  }

  private void tryPerformBeforeAction() {
    final FencedLock lock = this.getBeforeActionPerformerLock();
    final IAtomicReference<Boolean> isActionPerformed = this.getIsBeforeActionPerformed(); // NOPMD
    isActionPerformed.alter(p -> p != null && p); // p -> p == null ? false : p
    boolean triedPerformingBeforeAction = false;
    while (!isActionPerformed.get()) {
      // Try performing the before action
      triedPerformingBeforeAction = true;
      if (lock.tryLock()) {
        try {
          if (!isActionPerformed.get()) {
            LOGGER.info("This instance is elected to perform the before action.");
            this.loadGeneratorConfig.getBeforeAction().run();
            LOGGER.info("Before action performed.");
            isActionPerformed.set(true);
          }
        } finally {
          lock.unlock();
        }
      } else {
        LOGGER.info("Wait for before action to be performed.");
        delay(BEFORE_ACTION_WAIT_DURATION);
      }
    }
    if (!triedPerformingBeforeAction) {
      LOGGER.info("Before action has already been performed.");
    }
  }



  private void tryCreateTaskAssignment() {
    final Map<UUID, WorkloadDefinition> taskAssignment = this.getTaskAssignment();
    final FencedLock lock = this.getTaskAssignmentLock();

    boolean triedCreatingTaskAssignment = false;
    while (taskAssignment.size() != this.members.size()) {
      // Try creating task assignment
      triedCreatingTaskAssignment = true;
      if (lock.tryLock()) {
        try {
          if (taskAssignment.size() != this.members.size()) {
            LOGGER.info("This instance is elected to create the task assignment.");

            final Set<WorkloadDefinition> subLoadDefinitions =
                this.totalLoadDefinition.divide(this.members.size());
            Streams
                .zip(
                    subLoadDefinitions.stream(),
                    this.members.stream(),
                    (loadDef, member) -> new LoadDefPerMember(loadDef, member))
                .forEach(l -> taskAssignment.put(l.member.getUuid(), l.loadDefinition));

            LOGGER.info("Task assignment created.");
          }
        } finally {
          lock.unlock();
        }
      } else {
        LOGGER.info("Wait for task assignment to be available.");
        delay(TASK_ASSIGNMENT_WAIT_DURATION);
      }
    }
    if (!triedCreatingTaskAssignment) {
      LOGGER.info("Task assignment is already available.");
    }
  }

  private void startLoadGeneration() {
    if (this.loadGeneratorExecution != null) {
      throw new IllegalStateException("Load generation has already started before.");
    }
    LOGGER.info("Start running load generation and pick assigned task.");

    final Member member = this.hzInstance.getCluster().getLocalMember();
    final WorkloadDefinition workload = this.getTaskAssignment().get(member.getUuid());

    LOGGER.info("Run load generation for assigned task: {}", workload);
    this.loadGeneratorExecution = this.loadGeneratorConfig.buildLoadGeneratorExecution(workload);
    this.loadGeneratorExecution.start();
  }

  private void stopLoadGeneration() {
    this.loadGeneratorExecution.stop();
  }

  private IAtomicReference<Boolean> getIsBeforeActionPerformed() {
    return this.hzInstance.getCPSubsystem().getAtomicReference("isBeforeActionPerformed");
  }

  private FencedLock getBeforeActionPerformerLock() {
    return this.hzInstance.getCPSubsystem().getLock("beforeActionPerformer");
  }

  private Map<UUID, WorkloadDefinition> getTaskAssignment() {
    return this.hzInstance.getReplicatedMap(this.getTaskAssignmentName());
  }

  private FencedLock getTaskAssignmentLock() {
    return this.hzInstance.getCPSubsystem().getLock(this.getTaskAssignmentName() + "_assigner");
  }

  private String getTaskAssignmentName() {
    return this.members.stream()
        .map(m -> m.getUuid().toString())
        .collect(Collectors.joining("/"));
  }

  private static void delay(final Duration duration) {
    try {
      TimeUnit.MILLISECONDS.sleep(duration.toMillis());
    } catch (final InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  private static final class LoadDefPerMember {
    public final WorkloadDefinition loadDefinition; // NOCS used only internally
    public final Member member; // NOCS used only internally

    public LoadDefPerMember(final WorkloadDefinition loadDefinition, final Member member) {
      this.loadDefinition = loadDefinition;
      this.member = member;
    }
  }

}
