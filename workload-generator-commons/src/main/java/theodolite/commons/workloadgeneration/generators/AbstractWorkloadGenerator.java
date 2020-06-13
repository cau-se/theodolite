package theodolite.commons.workloadgeneration.generators;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import kieker.common.record.IMonitoringRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theodolite.commons.workloadgeneration.communication.zookeeper.WorkloadDistributor;
import theodolite.commons.workloadgeneration.dimensions.Duration;
import theodolite.commons.workloadgeneration.dimensions.KeySpace;
import theodolite.commons.workloadgeneration.dimensions.Period;
import theodolite.commons.workloadgeneration.functions.BeforeAction;
import theodolite.commons.workloadgeneration.functions.MessageGenerator;
import theodolite.commons.workloadgeneration.functions.Transport;
import theodolite.commons.workloadgeneration.misc.Worker;
import theodolite.commons.workloadgeneration.misc.WorkloadDefinition;
import theodolite.commons.workloadgeneration.misc.WorkloadEntity;
import theodolite.commons.workloadgeneration.misc.ZooKeeper;

public abstract class AbstractWorkloadGenerator<T extends IMonitoringRecord>
    implements WorkloadGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWorkloadGenerator.class);

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

  public AbstractWorkloadGenerator(
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
    this.workloadSelector = (workloadDefinition, worker) -> {
      final List<WorkloadEntity<T>> workloadEntities = new LinkedList<>();

      for (int i =
          workloadDefinition.getKeySpace().getMin() + worker.getId(); i <= workloadDefinition
              .getKeySpace().getMax(); i += workloadDefinition.getNumberOfWorkers()) {
        final String id = workloadDefinition.getKeySpace().getPrefix() + i;
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
