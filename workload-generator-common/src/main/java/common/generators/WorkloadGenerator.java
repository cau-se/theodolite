package common.generators;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.dimensions.Duration;
import common.dimensions.KeySpace;
import common.dimensions.Period;
import common.functions.BeforeAction;
import common.functions.MessageGenerator;
import common.functions.Transport;
import common.misc.Worker;
import common.misc.WorkloadDefinition;
import common.misc.WorkloadEntity;
import common.misc.ZooKeeper;
import communication.zookeeper.WorkloadDistributor;
import kieker.common.record.IMonitoringRecord;

public abstract class WorkloadGenerator<T extends IMonitoringRecord> implements IWorkloadGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkloadGenerator.class);

  private final ZooKeeper zooKeeper;

  private final KeySpace keySpace;

  private final int threads;

  private final Period period;

  private final Duration duration;

  private final BeforeAction beforeAction;

  private final BiFunction<WorkloadDefinition, Worker, List<WorkloadEntity<T>>> workloadSelector;

  private final MessageGenerator<T> generatorFunction;

  private final Transport<T> transport;

  private WorkloadDistributor workloadDistributor;

  private final ScheduledExecutorService executor;

  /**
   * Start the workload generation. The generation terminates automatically after the specified
   * {@code duration}.
   */
  @Override
  public void start() {
    this.workloadDistributor.start();
  }

  @Override
  public void stop() {
    this.workloadDistributor.stop();
  }

  public WorkloadGenerator(
      final ZooKeeper zooKeeper,
      final KeySpace keySpace,
      final int threads,
      final Period period,
      final Duration duration,
      final BeforeAction beforeAction,
      final MessageGenerator<T> generatorFunction,
      final Transport<T> transport) {
    this.zooKeeper = zooKeeper;
    this.period = period;
    this.threads = threads;
    this.keySpace = keySpace;
    this.duration = duration;
    this.beforeAction = beforeAction;
    this.generatorFunction = generatorFunction;
    this.workloadSelector = (workloadDeclaration, worker) -> {
      final List<WorkloadEntity<T>> workloadEntities = new LinkedList<>();

      for (int i =
          workloadDeclaration.getKeySpace().getMin() + worker.getId(); i <= workloadDeclaration
              .getKeySpace().getMax(); i += workloadDeclaration.getNumberOfWorkers()) {
        final String id = workloadDeclaration.getKeySpace().getPrefix() + i;
        workloadEntities.add(new WorkloadEntity<>(id, this.generatorFunction));
      }

      return workloadEntities;
    };
    this.transport = transport;

    this.executor = Executors.newScheduledThreadPool(threads);
    final Random random = new Random();

    final int periodMs = period.getPeriod();

    final BiConsumer<WorkloadDefinition, Worker> workerAction = (declaration, worker) -> {

      final List<WorkloadEntity<T>> entities = this.workloadSelector.apply(declaration, worker);

      LOGGER.info("Beginning of Experiment...");
      LOGGER.info("Experiment is going to be executed for the specified duration...");
      entities.forEach(entity -> {
        final T message = entity.generateMessage();
        final long initialDelay = random.nextInt(periodMs);
        this.executor.scheduleAtFixedRate(() -> this.transport.transport(message), initialDelay,
            periodMs, period.getTimeUnit());
      });

      try {
        this.executor.awaitTermination(duration.getDuration(), duration.getTimeUnit());
        LOGGER.info("Terminating now...");
        this.stop();
      } catch (final InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    };

    this.workloadDistributor =
        new WorkloadDistributor(this.zooKeeper, this.keySpace, this.beforeAction, workerAction);
  }
}
