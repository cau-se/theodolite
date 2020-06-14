package theodolite.commons.workloadgeneration.communication.zookeeper;

import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theodolite.commons.workloadgeneration.dimensions.KeySpace;
import theodolite.commons.workloadgeneration.functions.BeforeAction;
import theodolite.commons.workloadgeneration.misc.WorkloadDefinition;
import theodolite.commons.workloadgeneration.misc.ZooKeeper;

/*
 * The central class responsible for distributing the workload through all workload generators.
 */
public class WorkloadDistributor {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkloadDistributor.class);

  private static final String NAMESPACE = "workload-generation";
  private static final String COUNTER_PATH = "/counter";
  private static final String WORKLOAD_PATH = "/workload";
  private static final String WORKLOAD_DEFINITION_PATH = "/workload/definition";
  private final DistributedAtomicInteger counter;
  private final KeySpace keySpace;
  private final BeforeAction beforeAction;
  private final BiConsumer<WorkloadDefinition, Integer> workerAction;

  private final int instances;
  private final ZooKeeper zooKeeper;
  private final CuratorFramework client;

  private boolean workloadGenerationStarted = true;

  /**
   * Create a new workload distributor.
   *
   * @param keySpace the keyspace for the workload generation.
   * @param beforeAction the before action for the workload generation.
   * @param workerAction the action to perform by the workers.
   */
  public WorkloadDistributor(
      final int instances,
      final ZooKeeper zooKeeper,
      final KeySpace keySpace,
      final BeforeAction beforeAction,
      final BiConsumer<WorkloadDefinition, Integer> workerAction) {
    this.instances = instances;
    this.zooKeeper = zooKeeper;
    this.keySpace = keySpace;
    this.beforeAction = beforeAction;
    this.workerAction = workerAction;

    this.client = CuratorFrameworkFactory.builder()
        .namespace(NAMESPACE)
        .connectString(this.zooKeeper.getHost() + ":" + this.zooKeeper.getPort())
        .retryPolicy(new ExponentialBackoffRetry(2000, 5))
        .build();

    this.client.start();

    try {
      this.client.blockUntilConnected();
    } catch (final InterruptedException e) {
      LOGGER.error("", e);
      throw new IllegalStateException();
    }

    this.counter =
        new DistributedAtomicInteger(this.client, COUNTER_PATH,
            new ExponentialBackoffRetry(2000, 5));
  }

  /**
   * Start the workload distribution.
   */
  public void start() {
    try {
      AtomicValue<Integer> result = this.counter.increment();
      while (!result.succeeded()) {
        result = this.counter.increment();
      }

      final int workerId = result.preValue();

      final CuratorWatcher watcher = this.buildWatcher(workerId);

      this.client.checkExists().creatingParentsIfNeeded().forPath(WORKLOAD_PATH);

      if (workerId == 0) {
        LOGGER.info("This instance is master with id {}", workerId);

        this.beforeAction.run();

        // register worker action, as master acts also as worker
        this.client.getChildren().usingWatcher(watcher).forPath(WORKLOAD_PATH);

        LOGGER.info("Number of Workers: {}", this.instances);

        final WorkloadDefinition definition =
            new WorkloadDefinition(this.keySpace, this.instances);

        this.client.create().withMode(CreateMode.EPHEMERAL).forPath(WORKLOAD_DEFINITION_PATH,
            definition.toString().getBytes(StandardCharsets.UTF_8));

      } else {
        LOGGER.info("This instance is worker with id {}", workerId);

        this.client.getChildren().usingWatcher(watcher).forPath(WORKLOAD_PATH);

        final Stat exists =
            this.client.checkExists().creatingParentsIfNeeded().forPath(WORKLOAD_DEFINITION_PATH);

        if (exists != null) {
          this.startWorkloadGeneration(workerId);
        }
      }

      Thread.sleep(20_000);

    } catch (final Exception e) {
      LOGGER.error("", e);
      throw new IllegalStateException("Error when starting the distribution of the workload.");
    }
  }

  /**
   * Start the workload generation. This methods body does only get executed once.
   *
   * @param workerId the ID of this worker
   * @throws Exception
   */
  private synchronized void startWorkloadGeneration(final int workerId) throws Exception {
    if (!this.workloadGenerationStarted) {
      this.workloadGenerationStarted = true;

      final byte[] bytes =
          this.client.getData().forPath(WORKLOAD_DEFINITION_PATH);
      final WorkloadDefinition definition =
          WorkloadDefinition.fromString(new String(bytes, StandardCharsets.UTF_8));

      this.workerAction.accept(definition, workerId);
    }
  }

  /**
   * Build a curator watcher which performs the worker action.
   *
   * @param worker the worker to create the watcher for.
   * @return the curator watcher.
   */
  private CuratorWatcher buildWatcher(final int workerId) {
    return new CuratorWatcher() {

      @Override
      public void process(final WatchedEvent event) {
        if (event.getType() == EventType.NodeChildrenChanged) {
          try {
            WorkloadDistributor.this.startWorkloadGeneration(workerId);
          } catch (final Exception e) {
            LOGGER.error("", e);
            throw new IllegalStateException("Error starting workload generation.");
          }
        }
      }
    };
  }

  /**
   * Stop the workload distributor.
   */
  public void stop() {
    this.client.close();
  }

}
