package communication.zookeeper;

import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import common.dimensions.KeySpace;
import common.functions.BeforeAction;
import common.misc.Worker;
import common.misc.WorkloadDefinition;

/*
 * The central class responsible for distributing the workload through all workload generators.
 */
public class WorkloadDistributor {

  private static final String COUNTER_PATH = "/counter";
  private static final String WORKLOAD_PATH = "/workload";
  private static final String WORKLOAD_DEFINITION_PATH = "/workload/definition";

  private final DistributedAtomicInteger counter;

  private final KeySpace keySpace;
  private final BeforeAction beforeAction;
  private final BiConsumer<WorkloadDefinition, Worker> workerAction;

  private final CuratorFramework client =
      CuratorFrameworkFactory.newClient("127.0.0.1:2181", new RetryNTimes(3, 1000));

  /**
   * Create a new workload distributor.
   *
   * @param keySpace the keyspace for the workload generation.
   * @param beforeAction the before action for the workload generation.
   * @param workerAction the action to perform by the workers.
   */
  public WorkloadDistributor(final KeySpace keySpace, final BeforeAction beforeAction,
      final BiConsumer<WorkloadDefinition, Worker> workerAction) {

    this.keySpace = keySpace;
    this.beforeAction = beforeAction;
    this.workerAction = workerAction;

    this.client.start();

    try {
      this.client.blockUntilConnected();
    } catch (final InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    this.counter =
        new DistributedAtomicInteger(this.client, COUNTER_PATH, new RetryNTimes(3, 1000));
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

      final Worker worker = new Worker(result.preValue());

      final CuratorWatcher watcher = this.buildWatcher(worker);

      this.client.checkExists().creatingParentsIfNeeded().forPath(WORKLOAD_DEFINITION_PATH);

      if (worker.getId() == 0) {
        System.out.println("is master with id " + worker.getId());

        this.beforeAction.run();

        // register worker action, as master acts also as worker
        this.client.getChildren().usingWatcher(watcher).forPath(WORKLOAD_PATH);

        Thread.sleep(10000); // wait for all workers to participate in the leader election

        final int numberOfWorkers = this.counter.get().postValue();

        System.out.printf("Number of Workers: %d\n", numberOfWorkers);

        final WorkloadDefinition declaration =
            new WorkloadDefinition(this.keySpace, numberOfWorkers);

        this.client.create().withMode(CreateMode.EPHEMERAL).forPath(WORKLOAD_DEFINITION_PATH,
            declaration.toString().getBytes(StandardCharsets.UTF_8));

      } else {
        System.out.println("is worker with id " + worker.getId());

        this.client.getChildren().usingWatcher(watcher).forPath(WORKLOAD_PATH);
      }

      Thread.sleep(20000); // wait until the workload declaration is retrieved
    } catch (final Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Build a curator watcher which performs the worker action.
   *
   * @param worker the worker to create the watcher for.
   * @return the curator watcher.
   */
  private CuratorWatcher buildWatcher(final Worker worker) {
    return new CuratorWatcher() {

      @Override
      public void process(final WatchedEvent event) throws Exception {
        if (event.getType() == EventType.NodeChildrenChanged) {
          final byte[] bytes =
              WorkloadDistributor.this.client.getData().forPath(WORKLOAD_DEFINITION_PATH);
          final WorkloadDefinition declaration =
              WorkloadDefinition.fromString(new String(bytes, StandardCharsets.UTF_8));

          if (worker.getId() > declaration.getNumberOfWorkers() - 1) {
            throw new IllegalStateException("Worker with id " + worker.getId()
                + " was too slow and is therefore not participating in the workload generation.");
          } else {
            WorkloadDistributor.this.workerAction.accept(declaration, worker);
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
